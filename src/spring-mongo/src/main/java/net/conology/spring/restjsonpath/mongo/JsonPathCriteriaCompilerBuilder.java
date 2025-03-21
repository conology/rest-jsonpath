package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.AstCompilerPass;
import net.conology.restjsonpath.AstCompilerPassImpl;
import net.conology.restjsonpath.PostProcessor;
import net.conology.spring.restjsonpath.mongo.ir.MongoDelegatingValueAssertion;
import net.conology.spring.restjsonpath.mongo.ir.MongoSelector;
import net.conology.spring.restjsonpath.mongo.ir.MongoValueAssertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonPathCriteriaCompilerBuilder {

    private MongoValueAssertion existenceAssertion;
    private List<PostProcessor<MongoSelector>> mongoPostProcessors;
    private AstCompilerPass astCompilerPass;

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

    public JsonPathCriteriaCompilerBuilder astCompilerPass(AstCompilerPass astCompilerPass) {
        this.astCompilerPass = astCompilerPass;
        return this;
    }

    public JsonPathCriteriaCompiler build() {
        withDefaults();

        var mongoIrCompilerPassBuilder
            = new MongoIrCompilerPass.Builder()
                .existenceAssertion(existenceAssertion);

        return new JsonPathCriteriaCompiler(
            astCompilerPass,
            mongoIrCompilerPassBuilder,
            mongoPostProcessors
        );
    }

    private void withDefaults() {
        if (existenceAssertion == null) {
            existenceAssertion = MongoDelegatingValueAssertion.createDefaultExistenceAssertion();
        }
        if (astCompilerPass == null) {
            astCompilerPass = new AstCompilerPassImpl();
        }
        if (mongoPostProcessors == null) {
            mongoPostProcessors = Collections.emptyList();
        }
    }
}