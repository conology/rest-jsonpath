package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.ast.*;
import net.conology.spring.restjsonpath.mongo.ast.*;

import java.util.List;

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

    public MongoTestNode transformTestNode() {
        return compileTestNode(ir);
    }

    public MongoTestNode compileTestNode(PropertyFilterNode filterNode) {
        if (filterNode instanceof AndFilterNode andNode) {
            return compileAllOfTest(andNode);
        }
        return compilePropertyTest(filterNode);
    }

    private MongoTestNode compileAllOfTest(AndFilterNode andNode) {
        return new MongoAllOfTestNode(
            andNode.getNodes().stream()
                .map(this::compilePropertyTest)
                .toList()
        );
    }

    public MongoPropertyTest compilePropertyTest(PropertyFilterNode filterNode) {
        return switch (filterNode) {
            case RelativeValueComparingNode comparingFilter -> compilePropertyTest(comparingFilter);
            case ExistenceFilterNode existenceFilter -> compilePropertyTest(existenceFilter);
            case RegexFilterNode regexFilterNode -> compilePropertyTest(regexFilterNode);
            case AndFilterNode ignored ->
                throw new IllegalArgumentException("nested and expressions are not supported");
        };
    }
    private MongoPropertyTest compilePropertyTest(RegexFilterNode node) {
        var assertion = new RegexMongoValueAssertion(node);
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
        var assertion = new MongoValueComparingAssertion(
            node.getOperator(),
            node.getValueNode().getValue()
        );
        return new NestedValueTestCompiler(
            node.getRelativeQueryNode(),
            this,
            assertion
        ).compile();
    }


    public String normalizePath(List<String> path) {
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
                    : MongoDelegatingValueAssertion.createDefaultExistenceAssertion()
            );
        }

        public Builder existenceAssertion(MongoValueAssertion existenceAssertion) {
            this.existenceAssertion = existenceAssertion;
            return this;
        }
    }
}
