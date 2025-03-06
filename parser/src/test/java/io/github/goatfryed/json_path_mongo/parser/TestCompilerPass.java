package io.github.goatfryed.json_path_mongo.parser;

import io.github.goatfryed.json_path_mongo.JsonPathMongoLexer;
import io.github.goatfryed.json_path_mongo.JsonPathMongoParser;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;

public class TestCompilerPass {

    public static String parse(String input) {
        var lexer = new JsonPathMongoLexer(CharStreams.fromString(input));
        var parser = new JsonPathMongoParser(new BufferedTokenStream(lexer));
        return new TestCompilerPass().compile(parser.mongoQuery());
    }

    private String compile(JsonPathMongoParser.MongoQueryContext ir) {

        var sb = new StringBuilder();
        handleMongoQuery(ir, sb);

        return sb.toString();
    }

    private static void handleMongoQuery(JsonPathMongoParser.MongoQueryContext query, StringBuilder sb) {
        handleStartQuery(query, sb);
        handleAdditionalSegments(query, sb);
    }

    private static void handleAdditionalSegments(JsonPathMongoParser.MongoQueryContext query, StringBuilder sb) {
        for (var segment : query.segment()) {
            if (segment.memberNameShortHand() != null) {
                sb.append("->");
                sb.append(segment.memberNameShortHand().getText());
                continue;
            }
            if (segment.bracketedFilterSelector() != null) {
                var filter = segment.bracketedFilterSelector();
                var logicalExpression = filter.logicalExpression();

                sb.append("[having:");
                sb.append("]");
            }
        }
    }

    private static void handleStartQuery(JsonPathMongoParser.MongoQueryContext query, StringBuilder sb) {
        var startSegment = query.startSegment();
        var memberNameShortHand = startSegment.memberNameShortHand();
        if (memberNameShortHand != null) {
            sb.append(memberNameShortHand.getText());
            return;
        }
        throw new AssertionError();
    }
}
