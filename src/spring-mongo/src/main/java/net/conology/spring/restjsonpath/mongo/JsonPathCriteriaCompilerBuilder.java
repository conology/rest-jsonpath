package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.PostProcessor;
import net.conology.spring.restjsonpath.mongo.ir.MongoDelegatingValueAssertion;
import net.conology.spring.restjsonpath.mongo.ir.MongoSelector;
import net.conology.spring.restjsonpath.mongo.ir.MongoValueAssertion;
import net.conology.restjsonpath.core.parser.JsonPathMongoParser;
import org.antlr.v4.runtime.BailErrorStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class JsonPathCriteriaCompilerBuilder {

    private MongoValueAssertion existenceAssertion;
    private List<PostProcessor<MongoSelector>> mongoPostProcessors;

    private Consumer<JsonPathMongoParser> parserConfigurer = parser -> parser.setErrorHandler(new BailErrorStrategy());

    public JsonPathCriteriaCompilerBuilder parserConfigurer(Consumer<JsonPathMongoParser> parserConfigurer) {
        this.parserConfigurer = parserConfigurer;
        return this;
    }

    public JsonPathCriteriaCompilerBuilder existenceAssertion(MongoValueAssertion existenceAssertion) {
        this.existenceAssertion = existenceAssertion;
        return this;
    }

    public JsonPathCriteriaCompilerBuilder mongoSelectorPostProcessor(PostProcessor<MongoSelector> postProcessor) {
        if (mongoPostProcessors == null) {
            mongoPostProcessors = new ArrayList<>();
        }
        mongoPostProcessors.add(postProcessor);
        return this;
    }

    public JsonPathCriteriaCompiler build() {
        withDefaults();

        var mongoIrCompilerPassBuilder
            = new MongoIrCompilerPass.Builder()
                .existenceAssertion(existenceAssertion);

        return new JsonPathCriteriaCompiler(
            parserConfigurer, mongoIrCompilerPassBuilder,
            mongoPostProcessors
        );
    }

    private void withDefaults() {
        if (existenceAssertion == null) {
            existenceAssertion = MongoDelegatingValueAssertion.createDefaultExistenceAssertion();
        }
        if (parserConfigurer == null) {
            parserConfigurer = parser -> {
              parser.setErrorHandler(new BailErrorStrategy());
            };
        }
        if (mongoPostProcessors == null) {
            mongoPostProcessors = Collections.emptyList();
        }
    }
}