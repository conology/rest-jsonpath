package io.github.conology.jsonpath.mongo.spring;

import io.github.conology.jsonpath.core.JsonPathCompilerPass;
import io.github.conology.jsonpath.core.ast.PropertyFilterNode;
import io.github.conology.jsonpath.core.parser.JsonPathMongoLexer;
import io.github.conology.jsonpath.core.parser.JsonPathMongoParser;
import io.github.conology.jsonpath.mongo.spring.ast.MongoDelegatingValueAssertion;
import io.github.conology.jsonpath.mongo.spring.ast.MongoTestNode;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.function.Consumer;

public class JsonPathToCriteriaCompiler {

    private final MongoAstCompilerPass.Builder mongoAstCompilerPassBuilder;
    private final Consumer<JsonPathMongoParser> parserConfigurer;

    public JsonPathToCriteriaCompiler(
        MongoAstCompilerPass.Builder mongoAstCompilerPassBuilder,
        Consumer<JsonPathMongoParser> parserConfigurer
    ) {
        this.mongoAstCompilerPassBuilder = mongoAstCompilerPassBuilder;
        this.parserConfigurer = parserConfigurer;
    }

    public JsonPathToCriteriaCompiler() {
        this(
            new MongoAstCompilerPass.Builder()
                .existenceAssertion(MongoDelegatingValueAssertion.createDefaultExistenceAssertion()),
            parser -> parser.setErrorHandler(new BailErrorStrategy())
        );
    }

    public Criteria compile(String input) {
        var lexer = new JsonPathMongoLexer(CharStreams.fromString(input));
        var tokens = new BufferedTokenStream(lexer);
        var parser = new JsonPathMongoParser(tokens);
        parserConfigurer.accept(parser);

        var jsonPathIr = new JsonPathCompilerPass().getQueries(parser);

        var queries = jsonPathIr.stream()
            .map(this::toMongoIr)
            .map(this::toCriteria)
            .toList();

        if (queries.size() == 1) {
            return queries.getFirst();
        }

        return new Criteria().orOperator(queries);
    }

    private Criteria toCriteria(MongoTestNode mongoIr) {
        var critera = new Criteria();
        mongoIr.visit(critera);

        return critera;
    }

    private MongoTestNode toMongoIr(PropertyFilterNode filterNode) {
        return mongoAstCompilerPassBuilder
            .build(filterNode).transformTestNode();
    }
}
