package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.ast.*;
import net.conology.spring.restjsonpath.mongo.ast.*;

public class MongoIrCompilerPass {

    private final PropertyFilterNode ir;
    private final MongoValueAssertion existenceAssertion;

    public MongoIrCompilerPass(
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
        return compilePropertyTest(node.getRelativeQueryNode(), assertion);
    }

    private MongoPropertyTest compilePropertyTest(ExistenceFilterNode node) {
        return compilePropertyTest(node.getRelativeQueryNode(), null);
    }

    private MongoPropertyTest compilePropertyTest(RelativeValueComparingNode node) {
        var assertion = new MongoValueComparingAssertion(
            node.getOperator(),
            node.getValueNode().getValue()
        );
        return compilePropertyTest(node.getRelativeQueryNode(), assertion);
    }

    private MongoPropertyTest compilePropertyTest(RelativeQueryNode queryNode, MongoPropertyAssertion assertion) {
        return new NestedValueTestCompiler(
            queryNode,
            this,
            assertion
        ).compile();
    }

    public MongoValueAssertion getExistenceAssertion() {
        return existenceAssertion;
    }

    public static class Builder {
        private MongoValueAssertion existenceAssertion;

        public MongoIrCompilerPass build(PropertyFilterNode ir) {
            return new MongoIrCompilerPass(
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
