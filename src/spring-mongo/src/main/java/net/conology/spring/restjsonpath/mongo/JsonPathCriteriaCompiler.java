package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.InvalidQueryException;
import net.conology.restjsonpath.PostProcessor;
import net.conology.restjsonpath.JsonPathCompilerPass;
import net.conology.restjsonpath.ast.PropertyFilterNode;
import net.conology.restjsonpath.core.parser.JsonPathMongoLexer;
import net.conology.restjsonpath.core.parser.JsonPathMongoParser;
import net.conology.spring.restjsonpath.mongo.ir.MongoSelector;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.springframework.data.mongodb.InvalidMongoDbApiUsageException;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.function.Consumer;

public class JsonPathCriteriaCompiler {

    private final MongoIrCompilerPass.Builder mongoIrCompilerPassBuilder;
    private final List<PostProcessor<MongoSelector>> mongoPostProcessors;
    private final Consumer<JsonPathMongoParser> parserConfigurer;

    public JsonPathCriteriaCompiler(
        Consumer<JsonPathMongoParser> parserConfigurer, MongoIrCompilerPass.Builder mongoIrCompilerPassBuilder,
        List<PostProcessor<MongoSelector>> mongoPostProcessors
    ) {
        this.mongoIrCompilerPassBuilder = mongoIrCompilerPassBuilder;
        this.mongoPostProcessors = mongoPostProcessors;
        this.parserConfigurer = parserConfigurer;
    }

    public Criteria compile(String input) {
        try {
            return compileUnsafe(input);
        } catch (AssertionError error){
            throw error;
        } catch (
            IllegalStateException
            | InvalidMongoDbApiUsageException
            cause
        ) {
            throw new InvalidQueryException(cause);
        }
    }

    private Criteria compileUnsafe(String input) {
        var lexer = new JsonPathMongoLexer(CharStreams.fromString(input));
        var tokens = new BufferedTokenStream(lexer);
        var parser = new JsonPathMongoParser(tokens);
        parserConfigurer.accept(parser);

        var jsonPathIr = new JsonPathCompilerPass().getQueries(parser);

        var queries = jsonPathIr.stream()
            .map(this::toMongoIr)
            .map(MongoSelector::asCriteria)
            .toList();

        if (queries.size() == 1) {
            return queries.getFirst();
        }

        return new Criteria().orOperator(queries);
    }

    private MongoSelector toMongoIr(PropertyFilterNode filterNode) {
        var ir = mongoIrCompilerPassBuilder
            .build(filterNode)
            .transformTestNode();

        for (var visitor : mongoPostProcessors) {
            visitor.accept(ir);
        }

        return ir;
    }
}
