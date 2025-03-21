package net.conology.restjsonpath;

import net.conology.restjsonpath.core.parser.JsonPathMongoParser;
import net.conology.restjsonpath.core.parser.JsonPathMongoLexer;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;

public class AstCompilerPassImpl implements AstCompilerPass {

    @Override
    public JsonPathMongoParser transformToParserRepresentation(String input) {
        var lexer = transformToLexerRepresentation(CharStreams.fromString(input));
        return transformToParserRepresentation(new BufferedTokenStream(lexer));
    }

    protected JsonPathMongoParser transformToParserRepresentation(BufferedTokenStream tokens) {
        var jsonPathMongoParser = new JsonPathMongoParser(tokens);
        jsonPathMongoParser.setErrorHandler(new BailErrorStrategy());
        jsonPathMongoParser.removeErrorListeners();
        jsonPathMongoParser.addErrorListener(new StrictErrorListener());
        return jsonPathMongoParser;
    }

    protected JsonPathMongoLexer transformToLexerRepresentation(CodePointCharStream charStream) {
        var lexer = new JsonPathMongoLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new StrictErrorListener());
        return lexer;
    }

}
