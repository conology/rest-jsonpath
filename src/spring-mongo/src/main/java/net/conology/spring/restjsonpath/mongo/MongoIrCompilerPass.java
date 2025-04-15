package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.InvalidQueryException;
import net.conology.restjsonpath.ast.*;
import net.conology.spring.restjsonpath.mongo.ir.*;

public class MongoIrCompilerPass {

    private final PropertyFilterNode ir;
    private final MongoValueAssertion existenceAssertion;

    public MongoIrCompilerPass(
            PropertyFilterNode ir,
            MongoValueAssertion existenceAssertion) {
        this.ir = ir;
        this.existenceAssertion = existenceAssertion;
    }

    public MongoSelector transformTestNode() {
        return compileTestNode(ir);
    }

    public MongoSelector compileTestNode(PropertyFilterNode filterNode) {
        if (filterNode instanceof AndFilterNode andNode) {
            return compileAllOfTest(andNode);
        }
        return compilePropertyTest(filterNode);
    }

    private MongoSelector compileAllOfTest(AndFilterNode andNode) {
        return new MongoAllOfSelector(
                andNode.getNodes().stream()
                        .map(this::compilePropertyTest)
                        .toList());
    }

    public MongoPropertyCondition compilePropertyTest(PropertyFilterNode filterNode) {
        return switch (filterNode) {
            case RelativeValueComparingNode comparingFilter -> compilePropertyTest(comparingFilter);
            case ExistenceFilterNode existenceFilter -> compilePropertyTest(existenceFilter);
            case RegexFilterNode regexFilterNode -> compilePropertyTest(regexFilterNode);

            // TODO
            case OrFilterNode ignored ->
                throw new InvalidQueryException("nested or expressions are not supported");
            case AndFilterNode ignored ->
                throw new InvalidQueryException("nested and expressions are not supported");
        };
    }

    private MongoPropertyCondition compilePropertyTest(RegexFilterNode node) {
        var assertion = new RegexMongoValueAssertion(node);
        return compilePropertyTest(node.getRelativeQueryNode(), assertion);
    }

    private MongoPropertyCondition compilePropertyTest(ExistenceFilterNode node) {
        return compilePropertyTest(node.getRelativeQueryNode(), null);
    }

    private MongoPropertyCondition compilePropertyTest(RelativeValueComparingNode node) {
        var assertion = new MongoValueComparingAssertion(
                node.getOperator(),
                node.getValueNode().getValue());
        return compilePropertyTest(node.getRelativeQueryNode(), assertion);
    }

    private MongoPropertyCondition compilePropertyTest(RelativeQueryNode queryNode, MongoPropertyAssertion assertion) {
        return new NestedValueTestCompiler(
                queryNode,
                this,
                assertion).compile();
    }

    public MongoValueAssertion getExistenceAssertion() {
        return existenceAssertion;
    }

    public static class Builder {
        private MongoValueAssertion existenceAssertion;

        public MongoIrCompilerPass build(PropertyFilterNode ir) {
            return new MongoIrCompilerPass(
                    ir,
                    existenceAssertion != null ? existenceAssertion
                            : MongoDelegatingValueAssertion.createDefaultExistenceAssertion());
        }

        public Builder existenceAssertion(MongoValueAssertion existenceAssertion) {
            this.existenceAssertion = existenceAssertion;
            return this;
        }
    }
}
