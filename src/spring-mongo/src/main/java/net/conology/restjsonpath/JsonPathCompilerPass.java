package net.conology.restjsonpath;

import net.conology.restjsonpath.ast.*;
import net.conology.restjsonpath.core.parser.JsonPathMongoParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.text.StringEscapeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonPathCompilerPass {

    public List<PropertyFilterNode> getQueries(JsonPathMongoParser parser) {
        return transformRestQueries(parser.restQueries());
    }

    private List<PropertyFilterNode> transformRestQueries(JsonPathMongoParser.RestQueriesContext ctx) {
        guardParserException(ctx);

        return ctx.restQuery().stream()
                .map(this::transform)
                .toList();
    }

    private PropertyFilterNode transform(JsonPathMongoParser.RestQueryContext restQueryContext) {
        guardParserException(restQueryContext);
        return transform(restQueryContext.restOrQuery());
    }

    private PropertyFilterNode transform(JsonPathMongoParser.RestOrQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restAndQuery().isEmpty()) {
            throw failParserLexerMismatch();
        }

        var expressions = ctx.restAndQuery()
                .stream()
                .map(this::transform)
                .toList();

        return expressions.size() == 1 ? expressions.getFirst()
                : new OrFilterNode(expressions);
    }

    private PropertyFilterNode transform(JsonPathMongoParser.RestAndQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restBasicQuery().isEmpty()) {
            throw failParserLexerMismatch();
        }

        var expressions = ctx.restBasicQuery()
                .stream()
                .map(this::transform)
                .toList();

        return expressions.size() == 1 ? expressions.getFirst()
                : new AndFilterNode(expressions);
    }

    public PropertyFilterNode transform(JsonPathMongoParser.RestBasicQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restExistenceQuery() != null) {
            return transform(ctx.restExistenceQuery());
        }

        if (ctx.restComparisonQuery() != null) {
            return transformComparison(ctx.restComparisonQuery());
        }

        throw failParserLexerMismatch();
    }

    private ExistenceFilterNode transform(JsonPathMongoParser.RestExistenceQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restRelativeQuery() != null) {
            var queryNode = transformRelativeQuery(ctx.restRelativeQuery());
            return new ExistenceFilterNode(queryNode);
        }

        throw failParserLexerMismatch();
    }

    private RelativeQueryNode transformRelativeQuery(JsonPathMongoParser.RestRelativeQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.simplifiedRelativeQuery() != null) {
            return transformRelativeQuery(ctx.simplifiedRelativeQuery());
        }

        if (ctx.relativeQuery() != null) {
            return transformRelativeQuery(ctx.relativeQuery());
        }

        throw failParserLexerMismatch();
    }

    private RelativeQueryNode transformRelativeQuery(JsonPathMongoParser.SimplifiedRelativeQueryContext ctx) {
        guardParserException(ctx);

        var segments = PeekingIterator.of(ctx.segment().iterator());

        var startSelector = transformRelativeQueryStartSelector(ctx, segments);
        var relativeQuery = new RelativeQueryNode();
        relativeQuery.addNode(startSelector);

        collectSelectorNodes(relativeQuery, segments);

        return relativeQuery;
    }

    private SelectorNode transformRelativeQueryStartSelector(JsonPathMongoParser.SimplifiedRelativeQueryContext ctx,
            PeekingIterator<JsonPathMongoParser.SegmentContext> segments) {
        if (ctx.memberNameShortHand() != null) {
            return transformPropertySelector(ctx.memberNameShortHand(), segments);
        }

        if (ctx.bracketedExpression() != null) {
            return transformSelectorNode(ctx.bracketedExpression());
        }

        throw failParserLexerMismatch();
    }

    private RelativeQueryNode transformRelativeQuery(JsonPathMongoParser.RelativeQueryContext ctx) {
        guardParserException(ctx);

        var relativeQuery = new RelativeQueryNode();
        collectSelectorNodes(relativeQuery, PeekingIterator.of(ctx.segment().iterator()));

        return relativeQuery;
    }

    private void collectSelectorNodes(
            RelativeQueryNode relativeQuery,
            PeekingIterator<JsonPathMongoParser.SegmentContext> segments) {
        while (segments.hasNext()) {
            var next = segments.next();
            guardParserException(next);

            if (next.memberNameShortHand() != null) {
                var propertySelector = transformPropertySelector(
                        next.memberNameShortHand(),
                        segments);
                relativeQuery.addNode(propertySelector);
                continue;
            }

            if (next.bracketedExpression() != null) {
                var bracketedExpression = next.bracketedExpression();
                var selectorNode = transformSelectorNode(bracketedExpression);
                relativeQuery.addNode(selectorNode);
                continue;
            }

            throw failParserLexerMismatch();
        }
    }

    private SelectorNode transformSelectorNode(
            JsonPathMongoParser.BracketedExpressionContext ctx) {
        guardParserException(ctx);

        if (ctx.filterSelector() != null) {
            return transform(ctx.filterSelector());
        }

        if (ctx.WILDCARD_SELECTOR() != null) {
            return SelectorNode.Constant.WILDCARD;
        }

        if (ctx.INT() != null) {
            var index = Integer.parseInt(ctx.INT().getText());
            return new IndexSelectorNode(index);
        }

        if (ctx.QUOTED_TEXT() != null) {
            return transformUnsafeFieldSelector(ctx.QUOTED_TEXT());
        }

        throw failParserLexerMismatch();
    }

    private UnsafeFieldSelector transformUnsafeFieldSelector(TerminalNode quotedText) {
        var fieldName = processQuotedText(quotedText.getText());
        return new UnsafeFieldSelector(fieldName);
    }

    private FieldSelectorNode transformPropertySelector(
            JsonPathMongoParser.MemberNameShortHandContext ctx,
            PeekingIterator<JsonPathMongoParser.SegmentContext> segments) {
        guardParserException(ctx);
        return transformPropertySelector(
                ctx.SAFE_IDENTIFIER().getText(),
                segments);
    }

    private FieldSelectorNode transformPropertySelector(
            String startField,
            PeekingIterator<JsonPathMongoParser.SegmentContext> segments) {
        var path = new ArrayList<String>();
        path.add(startField);
        collectPropertySelectorPath(path, segments);
        return new FieldSelectorNode(path);
    }

    private PropertyFilterNode transformComparison(JsonPathMongoParser.RestComparisonQueryContext ctx) {
        guardParserException(ctx);

        if (ctx.restRelativeQuery() == null) {
            throw failParserLexerMismatch();
        }

        var leftNode = transformRelativeQuery(ctx.restRelativeQuery());

        if (ctx.regexComparison() != null) {
            return transformRegexComparison(leftNode, ctx.regexComparison());
        }

        if (ctx.literal() != null && ctx.comparisonOperator() != null) {
            return transformComparison(leftNode, ctx.literal(), ctx.comparisonOperator());
        }

        throw failParserLexerMismatch();
    }

    private PropertyFilterNode transformComparison(JsonPathMongoParser.ComparisonExpressionContext ctx) {
        guardParserException(ctx);

        if (ctx.relativeQuery() == null) {
            throw failParserLexerMismatch();
        }
        var leftNode = transformRelativeQuery(ctx.relativeQuery());

        if (ctx.regexComparison() != null) {
            return transformRegexComparison(leftNode, ctx.regexComparison());
        }

        if (ctx.literal() != null && ctx.comparisonOperator() != null) {
            return transformComparison(leftNode, ctx.literal(), ctx.comparisonOperator());
        }

        throw failParserLexerMismatch();
    }

    private RelativeValueComparingNode transformComparison(
            RelativeQueryNode propertyQuery,
            JsonPathMongoParser.LiteralContext literal,
            JsonPathMongoParser.ComparisonOperatorContext operatorCtx) {
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

    private static final Pattern REGEX_PATTERN = Pattern.compile("^/(.*)/([a-z]*)$");

    private RegexFilterNode transformRegexComparison(RelativeQueryNode queryNode,
            JsonPathMongoParser.RegexComparisonContext ctx) {
        guardParserException(ctx);

        if (ctx.REGULAR_EXPRESSION() != null) {
            return transformRegexComparison(queryNode, ctx.REGEX_COMPARISON_OPERATOR(), ctx.REGULAR_EXPRESSION());
        }

        throw failParserLexerMismatch();
    }

    private static RegexFilterNode transformRegexComparison(
            RelativeQueryNode queryNode,
            TerminalNode operator,
            TerminalNode regex) {
        var isNegated = switch (operator.getText()) {
            case "=~" -> false;
            case "!~" -> true;
            case null, default -> throw failParserLexerMismatch();
        };

        var expression = regex.getText();
        var matcher = REGEX_PATTERN.matcher(expression);
        if (!matcher.matches()) {
            throw failParserLexerMismatch();
        }
        var pattern = matcher.group(1);
        var options = matcher.group(2)
                .chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toSet());
        return new RegexFilterNode(queryNode, pattern, options, isNegated);
    }

    private static void collectPropertySelectorPath(
            ArrayList<String> path,
            PeekingIterator<JsonPathMongoParser.SegmentContext> segments) {
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

        if (filterCtx.orExpression() != null) {
            return transform(filterCtx.orExpression());
        }

        throw failParserLexerMismatch();
    }

    private PropertyFilterNode transform(JsonPathMongoParser.OrExpressionContext ctx) {
        guardParserException(ctx);

        if (ctx.andExpression().isEmpty()) {
            throw failParserLexerMismatch();
        }

        var expressions = ctx.andExpression()
                .stream()
                .map(this::transform)
                .toList();

        return expressions.size() == 1 ? expressions.getFirst()
                : new OrFilterNode(expressions);
    }

    private PropertyFilterNode transform(JsonPathMongoParser.AndExpressionContext ctx) {
        guardParserException(ctx);

        if (ctx.logicalExpression().isEmpty()) {
            throw failParserLexerMismatch();
        }

        var expressions = ctx.logicalExpression()
                .stream()
                .map(this::transformLogicalExpression)
                .toList();

        return expressions.size() == 1 ? expressions.getFirst()
                : new AndFilterNode(expressions);
    }

    private PropertyFilterNode transformLogicalExpression(
            JsonPathMongoParser.LogicalExpressionContext logicalExpression) {
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
            JsonPathMongoParser.ExistenceExpressionContext ctx) {
        guardParserException(ctx);

        if (ctx.relativeQuery() != null) {
            return new ExistenceFilterNode(transformRelativeQuery(ctx.relativeQuery()));
        }

        throw failParserLexerMismatch();
    }

    private ValueNode transformLiteral(JsonPathMongoParser.LiteralContext literal) {
        guardParserException(literal);

        if (literal.INT() != null) {
            return transformInteger(literal.INT());
        }

        if (literal.FLOAT() != null) {
            try {
                var number = new BigDecimal(literal.FLOAT().getText());
                return new ValueNode(number);
            } catch (Exception e) {
                throw failParserLexerMismatch();
            }
        }

        if (literal.QUOTED_TEXT() != null) {
            return new ValueNode(processQuotedText(literal.QUOTED_TEXT().getText()));
        }

        if (literal.FALSE() != null) {
            return new ValueNode(false);
        }

        if (literal.TRUE() != null) {
            return new ValueNode(true);
        }

        if (literal.NULL() != null) {
            return new ValueNode(ValueNode.SPECIAL_VALUE.NULL);
        }

        throw failParserLexerMismatch();
    }

    private ValueNode transformInteger(TerminalNode literalInt) {
        var text = literalInt.getText();
        try {
            var number = Integer.parseInt(text);
            return new ValueNode(number);
        } catch (Exception e) {
            var number = new BigInteger(text);
            return new ValueNode(number);
        }
    }

    private String processQuotedText(String quotedText) {
        var inner = quotedText.substring(1, quotedText.length() - 1);

        return StringEscapeUtils.unescapeJson(inner);
    }

    private static void guardParserException(ParserRuleContext ctx) {
        if (ctx.exception != null) {
            throw new IllegalArgumentException(ctx.exception);
        }
    }

    private static AssertionError failParserLexerMismatch() {
        return new AssertionError(
                "unexpected parser state. This indicates a language version mismatch");
    }
}
