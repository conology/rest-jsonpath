package io.github.conology.jsonpath.core;

import io.github.conology.jsonpath.core.ast.*;
import io.github.conology.jsonpath.core.parser.JsonPathMongoLexer;
import io.github.conology.jsonpath.core.parser.JsonPathMongoParser;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.LinkedList;

public class JsonPathCompilerPass {

    public static PropertyFilterNode parseRestQuery(String input) {
        var lexer = new JsonPathMongoLexer(CharStreams.fromString(input));
        var parser = new JsonPathMongoParser(new BufferedTokenStream(lexer));

        return new JsonPathCompilerPass().transform(parser.restQuery());
    }

    private PropertyFilterNode transform(JsonPathMongoParser.RestQueryContext restQueryContext) {
        guardParserException(restQueryContext);
        return transform(restQueryContext.restBasicQuery());
    }

    public PropertyFilterNode transform(JsonPathMongoParser.RestBasicQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restExistenceQuery() != null) {
            return transform(ctx.restExistenceQuery());
        }

        if (ctx.restComparisonQuery() != null) {
            return transform(ctx.restComparisonQuery());
        }

        throw failParserLexerMismatch();
    }

    private ExistenceFilterNode transform(JsonPathMongoParser.RestExistenceQueryContext ctx) {
        var relativeQueryNode = transformRelativeQuery(ctx);
        return new ExistenceFilterNode(relativeQueryNode);
    }

    private RelativeQueryNode transformRelativeQuery(JsonPathMongoParser.RestExistenceQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restShortRelativeQuery() != null) {
            return transformRelativeQuery(ctx.restShortRelativeQuery());
        }

        if (ctx.relativeQuery() != null) {
            return transformRelativeQuery(ctx.relativeQuery());
        }

        throw failParserLexerMismatch();
    }

    private RelativeQueryNode transformRelativeQuery(JsonPathMongoParser.RestShortRelativeQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restMemberSelector() == null) {
            throw new AssertionError("Unexpected parser state. RestMemberSelector required");
        }

        var segments = PeekingIterator.of(ctx.segment().iterator());

        var relativeQuery = new RelativeQueryNode();
        var propertySelector = transformPropertySelector(relativeQuery, ctx.restMemberSelector(), segments);
        relativeQuery.addNode(propertySelector);

        collectSelectorNodes(relativeQuery, segments);

        return relativeQuery;
    }

    private RelativeQueryNode transformRelativeQuery(JsonPathMongoParser.RelativeQueryContext ctx) {
        guardParserException(ctx);

        var relativeQuery = new RelativeQueryNode();
        collectSelectorNodes(relativeQuery, PeekingIterator.of(ctx.segment().iterator()));

        return relativeQuery;
    }

    private void collectSelectorNodes(
        RelativeQueryNode relativeQuery,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        while (segments.hasNext()) {
            var next = segments.next();
            guardParserException(next);

            if (next.memberNameShortHand() != null) {
                var propertySelector = transformPropertySelector(
                    relativeQuery,
                    next.memberNameShortHand(),
                    segments
                );
                relativeQuery.addNode(propertySelector);
                continue;
            }

            if (next.bracketedExpression() != null) {
                var bracketedExpression = next.bracketedExpression();
                transformBracketedExpression(relativeQuery, bracketedExpression);
                continue;
            }


            throw failParserLexerMismatch();
        }
    }

    private void transformBracketedExpression(RelativeQueryNode relativeQuery, JsonPathMongoParser.BracketedExpressionContext bracketedExpression) {
        guardParserException(bracketedExpression);

        if (bracketedExpression.filterSelector() != null) {
            var filterExpression = transform(bracketedExpression.filterSelector());
            relativeQuery.addNode(filterExpression);
            return;
        }

        if (bracketedExpression.WILDCARD_SELECTOR() != null) {
            relativeQuery.addNode(SelectorNode.Constant.WILDCARD);
            return;
        }

        if (bracketedExpression.INT() != null) {
            var index = Integer.parseInt(bracketedExpression.INT().getText());
            relativeQuery.addNode(new IndexSelectorNode(index));
            return;
        }

        throw failParserLexerMismatch();
    }

    private FieldSelectorNode transformPropertySelector(
        RelativeQueryNode relativeQuery,
        JsonPathMongoParser.MemberNameShortHandContext ctx,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        guardParserException(ctx);
        return transformPropertySelector(
            relativeQuery,
            ctx.SAFE_IDENTIFIER().getText(),
            segments
        );
    }

    private FieldSelectorNode transformPropertySelector(
        RelativeQueryNode relativeQuery,
        JsonPathMongoParser.RestMemberSelectorContext ctx,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        guardParserException(ctx);
        return transformPropertySelector(
            relativeQuery,
            ctx.SAFE_IDENTIFIER().getText(),
            segments
        );
    }

    private FieldSelectorNode transformPropertySelector(
        RelativeQueryNode relativeQuery,
        String startField,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        var path = new LinkedList<String>();
        path.add(startField);
        collectPropertySelectorPath(path, segments);
        return new FieldSelectorNode(path, relativeQuery);
    }

    private PropertyFilterNode transform(JsonPathMongoParser.RestComparisonQueryContext ctx) {
        guardParserException(ctx);

        var leftNode = transformRelativeQuery(ctx.restShortRelativeQuery());
        return transformComparison(leftNode, ctx.literal(), ctx.comparisonOperator());
    }

    private RelativeValueComparingNode transformComparison(JsonPathMongoParser.ComparisonExpressionContext comparison) {
        guardParserException(comparison);

        var leftNode = transformRelativeQuery(comparison.relativeQuery());
        return transformComparison(leftNode, comparison.literal(), comparison.comparisonOperator());
    }

    private RelativeValueComparingNode transformComparison(
        RelativeQueryNode propertyQuery,
        JsonPathMongoParser.LiteralContext literal,
        JsonPathMongoParser.ComparisonOperatorContext operatorCtx
    ) {
        var valueNode = transformLiteral(literal);

        var operator = switch (operatorCtx.getText()) {
            case "==" -> ComparisonOperator.EQ;
            case "!=" -> ComparisonOperator.NEQ;
            case ">" -> ComparisonOperator.GT;
            case ">=" -> ComparisonOperator.GTE;
            case "<" -> ComparisonOperator.LT;
            case "<=" -> ComparisonOperator.LTE;
            default -> throw new AssertionError();
        };

        return new RelativeValueComparingNode(propertyQuery, valueNode, operator);
    }

    private static void collectPropertySelectorPath(
        LinkedList<String> path,
        PeekingIterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        while (segments.hasNext()) {
            var segment = segments.peek();
            guardParserException(segment);
            if (segment.memberNameShortHand() != null) {
                segments.next();
                var shortHand = segment.memberNameShortHand();
                guardParserException(shortHand);
                path.add(shortHand.SAFE_IDENTIFIER().getText());
            } else {
                return;
            }
        }
    }

    private PropertyFilterNode transform(JsonPathMongoParser.FilterSelectorContext filterCtx) {
        guardParserException(filterCtx);
        
        if (filterCtx.logicalExpression() != null) {
            return transformLogicalExpression(filterCtx.logicalExpression());
        }
        
        throw failParserLexerMismatch();
    }

    private PropertyFilterNode transformLogicalExpression(JsonPathMongoParser.LogicalExpressionContext logicalExpression) {
        guardParserException(logicalExpression);

        if (logicalExpression.comparisonExpression() != null) {
            return transformComparison(logicalExpression.comparisonExpression());
        }

        if (logicalExpression.existenceExpression() != null) {
            return transformExistenceExpression(logicalExpression.existenceExpression());
        }

        throw failParserLexerMismatch();
    }

    private ExistenceFilterNode transformExistenceExpression(
        JsonPathMongoParser.ExistenceExpressionContext ctx
    ) {
        guardParserException(ctx);
        
        if (ctx.relativeQuery() != null) {
            return new ExistenceFilterNode(transformRelativeQuery(ctx.relativeQuery()));
        }
        
        throw failParserLexerMismatch();
    }

    private ValueNode transformLiteral(JsonPathMongoParser.LiteralContext literal) {
        guardParserException(literal);
        if (literal.INT() != null) {
            var number = Integer.valueOf(literal.INT().getText());
            return new ValueNode(number);
        }
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
