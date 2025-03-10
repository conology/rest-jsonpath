package io.github.conology.jsonpath.core;

import io.github.conology.jsonpath.core.parser.JsonPathMongoLexer;
import io.github.conology.jsonpath.core.parser.JsonPathMongoParser;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Objects;

public class JsonPathCompilerPass {

    public static QueryNode parse(String input) {
        var lexer = new JsonPathMongoLexer(CharStreams.fromString(input));
        var parser = new JsonPathMongoParser(new BufferedTokenStream(lexer));

        return new JsonPathCompilerPass().compile(parser.restQuery());
    }

    private QueryNode compile(JsonPathMongoParser.RestQueryContext restQueryContext) {
        guardParserException(restQueryContext);
        return compile(restQueryContext.restBasicQuery());
    }

    public QueryNode compile(JsonPathMongoParser.RestBasicQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restExistenceQuery() != null) {
            return compile(ctx.restExistenceQuery());
        }

        if (ctx.restComparisonQuery() != null) {
            return compile(ctx.restComparisonQuery());
        }

        throw failParserLexerMismatch();
    }

    private PropertyQuery compile(JsonPathMongoParser.RestExistenceQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restShortRelativeQuery() != null) {
            return compile(ctx.restShortRelativeQuery());
        }

        if (ctx.relativeQuery() != null) {
            return compile(ctx.relativeQuery());
        }

        throw failParserLexerMismatch();
    }

    private PropertyQuery compile(JsonPathMongoParser.RestShortRelativeQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restMemberSelector() == null) {
            throw new AssertionError("Unexpected parser state. RestMemberSelector required");
        }

        var propSelector = new PropertyQueryImpl();
        propSelector.appendField(ctx.restMemberSelector().getText());

        if (!ctx.segment().isEmpty()) {
            collectPropertyQuery(propSelector, PeekingIterator.of(ctx.segment().iterator()));
        }

        return propSelector;
    }

    private PropertyQuery compile(JsonPathMongoParser.RelativeQueryContext ctx) {
        guardParserException(ctx);

        var segments = PeekingIterator.of(ctx.segment().iterator());
        return compilePropertyQuery(segments.next(), segments);
    }

    private void collectPropertyQuery(
        PropertyQueryImpl propertySelector,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        collectPropertyQueryPath(propertySelector, segments);
        collectPropertyQueryFilter(propertySelector, segments);

        if (segments.hasNext()) {
            var childSelector = compilePropertyQuery(segments.next(), segments);
            propertySelector.setChildSelector(childSelector);
        }
    }

    private QueryNode compile(JsonPathMongoParser.RestComparisonQueryContext ctx) {
        guardParserException(ctx);

        var leftNode = compile(ctx.restShortRelativeQuery());
        return compileComparison(leftNode, ctx.literal(), ctx.comparisonOperator());
    }

    private ComparingQuery compileComparison(JsonPathMongoParser.ComparisonExpressionContext comparison) {
        guardParserException(comparison);

        var leftNode = compile(comparison.relativeQuery());
        return compileComparison(leftNode, comparison.literal(), comparison.comparisonOperator());
    }

    private ComparingQuery compileComparison(
        PropertyQuery propertyQuery,
        JsonPathMongoParser.LiteralContext literal,
        JsonPathMongoParser.ComparisonOperatorContext operatorCtx
    ) {
        var rightNode = compile(literal);

        var operator = switch (operatorCtx.getText()) {
            case "==" -> ComparisonOperator.EQ;
            case "!=" -> ComparisonOperator.NEQ;
            case ">" -> ComparisonOperator.GT;
            case ">=" -> ComparisonOperator.GTE;
            case "<" -> ComparisonOperator.LT;
            case "<=" -> ComparisonOperator.LTE;
            default -> throw new AssertionError();
        };

        return new ComparingQuery(propertyQuery, rightNode, operator);
    }

    private PropertyQuery compilePropertyQuery(
        JsonPathMongoParser.SegmentContext startSegment,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        var propertySelector = new PropertyQueryImpl();
        propertySelector.appendField(startSegment.memberNameShortHand().SAFE_IDENTIFIER().getText());

        collectPropertyQuery(propertySelector, segments);
        return propertySelector;
    }


    private static void collectPropertyQueryPath(
        PropertyQueryImpl propertySelector,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        while (segments.hasNext()) {
            var segment = segments.peek();
            if (segment.memberNameShortHand() != null) {
                segments.next();
                var shortHand = segment.memberNameShortHand();
                guardParserException(shortHand);
                propertySelector.appendField(shortHand.SAFE_IDENTIFIER().getText());
            } else {
                return;
            }
        }
    }

    private void collectPropertyQueryFilter(
        PropertyQueryImpl propertySelector,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        while (segments.hasNext()) {
            var segment = segments.peek();
            if (segment.bracketedExpression() == null) break;

            var bracketedExpression = segment.bracketedExpression();
            if (bracketedExpression.filterQuery() == null) break;

            segments.next();
            var filterExpression = bracketedExpression.filterQuery();
            var filterNode = compile(filterExpression);
            propertySelector.addFilter(filterNode);
        }
    }

    private QueryNode compile(JsonPathMongoParser.FilterQueryContext filterCtx) {
        guardParserException(filterCtx);

        var logicalExpression = Objects.requireNonNull(filterCtx.logicalExpression());
        guardParserException(logicalExpression);

        var comparison = Objects.requireNonNull(logicalExpression.comparisonExpression());
        return compileComparison(comparison);
    }

    private ValueNode compile(JsonPathMongoParser.LiteralContext literal) {
        if (literal.QUOTED_TEXT() != null) {
            return new ValueNode(processQuotedText(literal.QUOTED_TEXT().getText()));
        }
        return new ValueNode(literal.INT().getText());
    }

    private String processQuotedText(String terminalNode) {
        return terminalNode
            .substring(1, terminalNode.length() - 1) // strip the quotes
        ;
    }


    private static void guardParserException(ParserRuleContext ctx) {
        if (ctx.exception != null) {
            throw new IllegalArgumentException(ctx.exception);
        }
    }

    private static AssertionError failParserLexerMismatch() {
        return new AssertionError(
            "unexpected parser state. This indicates a language version mismatch"
        );
    }
}
