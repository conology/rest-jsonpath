package io.github.conology.jsonpath.mongo.spring;

import io.github.conology.jsonpath.core.ast.*;
import io.github.conology.jsonpath.mongo.spring.ast.*;

import java.util.*;

public class MongoAstCompilerPass {

    private final PropertyFilterNode ir;
    private final MongoValueAssertion existenceAssertion;

    public MongoAstCompilerPass(
        PropertyFilterNode ir,
        MongoValueAssertion existenceAssertion
    ) {
        this.ir = ir;
        this.existenceAssertion = existenceAssertion;
    }

    public MongoPropertyTest compile() {
        return compile(ir);
    }

    public MongoPropertyTest compile(PropertyFilterNode filterNode) {
        return switch (filterNode) {
            case RelativeValueComparingNode comparingFilter -> compilePropertyTest(comparingFilter);
            case ExistenceFilterNode existenceFilter -> compilePropertyTest(existenceFilter);
            case RegexFilterNode regexFilterNode -> compilePropertyTest(regexFilterNode);
        };
    }

    private MongoPropertyTest compilePropertyTest(RegexFilterNode node) {
        var assertion = new RegexMongoValueAssertion(
            node.getRegexPattern(),
            node.getOptions()
        );
        return new NestedValueTestCompiler(
            node.getRelativeQueryNode(),
            this,
            assertion
        ).compile();
    }

    private MongoPropertyTest compilePropertyTest(ExistenceFilterNode node) {
        return new NestedValueTestCompiler(
            node.getRelativeQueryNode(),
            this,
            null
        ).compile();
    }

    private MongoPropertyTest compilePropertyTest(RelativeValueComparingNode node) {
        var assertion = new ComparingMongoValueAssertion(
            node.getOperator(),
            node.getValueNode().getValue()
        );
        return new NestedValueTestCompiler(
            node.getRelativeQueryNode(),
            this,
            assertion
        ).compile();
    }


    public String normalizePath(LinkedList<String> path) {
        return String.join(".", path);
    }

    public MongoValueAssertion getExistenceAssertion() {
        return existenceAssertion;
    }

    public static class Builder {
        private MongoValueAssertion existenceAssertion;

        public MongoAstCompilerPass build(PropertyFilterNode ir) {
            return new MongoAstCompilerPass(
                ir,
                existenceAssertion != null ?
                    existenceAssertion
                    : DelegatingMongoValueAssertion.createDefaultExistenceAssertion()
            );
        }

        public Builder existenceAssertion(MongoValueAssertion existenceAssertion) {
            this.existenceAssertion = existenceAssertion;
            return this;
        }
    }
}
