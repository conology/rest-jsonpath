package io.github.conology.jsonpath.core;

import io.github.conology.jsonpath.core.parser.JsonPathMongoLexer;
import io.github.conology.jsonpath.core.parser.JsonPathMongoParser;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Objects;

public class JsonPathCompilerPass {

    public static PropertyQuery parse(String input) {
        var lexer = new JsonPathMongoLexer(CharStreams.fromString(input));
        var parser = new JsonPathMongoParser(new BufferedTokenStream(lexer));

        return new JsonPathCompilerPass().compile(parser.restQuery());
    }

    private PropertyQuery compile(JsonPathMongoParser.RestQueryContext restQueryContext) {
        return compile(restQueryContext.restBasicQuery());
    }

    public PropertyQuery compile(JsonPathMongoParser.RestBasicQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restExistenceQuery() != null) {
            return compile(ctx.restExistenceQuery());
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

        throw failParserLexerMismatch();
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

    private FilterNode compile(JsonPathMongoParser.FilterQueryContext filterCtx) {
        guardParserException(filterCtx);

        var logicalExpression = Objects.requireNonNull(filterCtx.logicalExpression());
        guardParserException(logicalExpression);
        var comparison = Objects.requireNonNull(logicalExpression.comparisonExpression());
        guardParserException(comparison);

        var leftNode = processExpression(comparison.relativeQuery());
        var rightNode = processExpression(comparison.literal());

        var operator = switch (comparison.comparisonOperator().getText()) {
            case "==" -> ComparisonOperator.EQ;
            case "!=" -> ComparisonOperator.NEQ;
            case ">" -> ComparisonOperator.GT;
            case ">=" -> ComparisonOperator.GTE;
            case "<" -> ComparisonOperator.LT;
            case "<=" -> ComparisonOperator.LTE;
            default -> throw new AssertionError();
        };

        return new ComparingFilter(leftNode, rightNode, operator);
    }

    private ValueNode processExpression(JsonPathMongoParser.LiteralContext literal) {
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

    private PropertyQuery processExpression(JsonPathMongoParser.RelativeQueryContext relativeQuery) {
        var segments = PeekingIterator.of(relativeQuery.segment().iterator());
        return compilePropertyQuery(segments.next(), segments);
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
