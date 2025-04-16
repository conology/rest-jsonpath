package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.*;
import net.conology.restjsonpath.ast.PropertyFilterNode;
import net.conology.restjsonpath.core.parser.JsonPathMongoLexer;
import net.conology.restjsonpath.core.parser.JsonPathMongoParser;
import net.conology.spring.restjsonpath.mongo.ir.MongoAllOfSelector;
import net.conology.spring.restjsonpath.mongo.ir.MongoAnyOfSelector;
import net.conology.spring.restjsonpath.mongo.ir.MongoPropertyCondition;
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
    private final AstCompilerPass astCompilerPass;
    private final JsonPathCompilerPass jsonPathCompilerPass;

    public JsonPathCriteriaCompiler(
            AstCompilerPass astCompilerPass,
            MongoIrCompilerPass.Builder mongoIrCompilerPassBuilder,
            List<PostProcessor<MongoSelector>> mongoPostProcessors) {
        this.mongoIrCompilerPassBuilder = mongoIrCompilerPassBuilder;
        this.mongoPostProcessors = mongoPostProcessors;
        this.astCompilerPass = astCompilerPass;
        jsonPathCompilerPass = new JsonPathCompilerPass();
    }

    public Criteria compile(String input) {
        try {
            return compileUnsafe(input);
        } catch (AssertionError error) {
            throw error;
        } catch (
                IllegalStateException
                | InvalidMongoDbApiUsageException cause) {
            throw new InvalidQueryException(cause);
        }
    }

    private Criteria compileUnsafe(String input) {
        var astIr = astCompilerPass.transformToParserRepresentation(input);
        var jsonPathIr = jsonPathCompilerPass.getQueries(astIr);

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

        guardInvalidTopLevelQuery(ir);

        return ir;
    }

    private void guardInvalidTopLevelQuery(MongoSelector ir) {
        switch (ir) {
            case MongoAllOfSelector allOf -> guardInvalidTopLevelQuery(allOf);
            case MongoAnyOfSelector anyOf -> guardInvalidTopLevelQuery(anyOf);
            case MongoPropertyCondition condition -> guardInvalidTopLevelQuery(condition);
        }
    }

    private void guardInvalidTopLevelQuery(MongoAllOfSelector allOf) {
        // allOf.getTests().forEach(this::guardInvalidTopLevelQuery);
    }

    private void guardInvalidTopLevelQuery(MongoAnyOfSelector anyOf) {
        // anyOf.getTests().forEach(this::guardInvalidTopLevelQuery);
    }

    private void guardInvalidTopLevelQuery(MongoPropertyCondition condition) {
        if (condition.getPropertySelector().getPath().isEmpty()) {
            throw new InvalidQueryException("root query must always start with a property selection");
        }
    }
}
