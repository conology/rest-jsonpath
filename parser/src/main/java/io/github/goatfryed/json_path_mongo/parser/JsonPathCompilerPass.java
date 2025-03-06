package io.github.goatfryed.json_path_mongo.parser;

import io.github.goatfryed.json_path_mongo.JsonPathMongoLexer;
import io.github.goatfryed.json_path_mongo.JsonPathMongoParser;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;

import java.util.Iterator;

public class JsonPathCompilerPass {

    public PropertySelector compile(JsonPathMongoParser.MongoQueryContext queryContext) {

        var propertySelector = new PropertySelectorImpl();
        propertySelector.appendField(queryContext.startSegment().memberNameShortHand().getText());

        var segments = queryContext.segment().iterator();
        collectPropertySelector(propertySelector, segments);

        return propertySelector;
    }

    private void collectPropertySelector(
        PropertySelectorImpl propertySelector,
        Iterator<JsonPathMongoParser.SegmentContext> segments
    ) {
        PropertySelectorImpl parentPropertySelector = null;
        PropertySelectorImpl currentPropertySelector = propertySelector;

        while (segments.hasNext()) {
            var segment = segments.next();
            if (segment.memberNameShortHand() != null) {
                if (currentPropertySelector == null) {
                    assert parentPropertySelector != null;
                    currentPropertySelector = new PropertySelectorImpl();
                    parentPropertySelector.addCriteria(currentPropertySelector);
                }
                currentPropertySelector.appendField(segment.memberNameShortHand().getText());
                continue;
            }
            if (segment.bracketedFilterSelector() != null) {
                var propertyFilter = collectPropertyFilter(segment.bracketedFilterSelector());
                if (currentPropertySelector != null) {
                    parentPropertySelector = currentPropertySelector;
                    currentPropertySelector = null;
                }
                assert parentPropertySelector != null;
                parentPropertySelector.addCriteria(propertyFilter);
            }
        }
    }

    private PropertyFilter collectPropertyFilter(JsonPathMongoParser.BracketedFilterSelectorContext filterContext) {
        var comparison = filterContext.logicalExpression().comparisonExpression();

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

        return new PropertyFilter(leftNode, rightNode, operator);
    }

    private ValueNode processExpression(JsonPathMongoParser.LiteralContext literal) {
        if (literal.quotedText() != null) {
            return new ValueNode(literal.quotedText().quotedTextInner().getText());
        }
        return new ValueNode(literal.INT().getText());
    }

    private PropertySelector processExpression(JsonPathMongoParser.RelativeQueryContext relativeQuery) {
        var propertySelector = new PropertySelectorImpl();
        propertySelector.appendField(relativeQuery.memberNameShortHand().getText());
        collectPropertySelector(propertySelector, relativeQuery.segment().iterator());
        return propertySelector;
    }

    public static PropertySelector parse(String input) {
        var lexer = new JsonPathMongoLexer(CharStreams.fromString(input));
        var parser = new JsonPathMongoParser(new BufferedTokenStream(lexer));

        return new JsonPathCompilerPass().compile(parser.mongoQuery());
    }

}
