package net.conology.spring.restjsonpath.mongo;

import java.util.List;

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
        } else if (filterNode instanceof OrFilterNode orNode) {
            return compileAnyOfTest(orNode);
        }
        return compilePropertyTest(filterNode);
    }

    private MongoAllOfSelector compileAllOfTest(AndFilterNode andNode) {
        return new MongoAllOfSelector(
                andNode.getNodes().stream()
                        .map(this::compilePropertyTest)
                        .toList());
    }

    private MongoSelector compileAnyOfTest(OrFilterNode orNode) {
        return new MongoAnyOfSelector(
                orNode.getNodes().stream().map(node -> new MongoAllOfSelector(List.of(compilePropertyTest(node))))
                        .toList());

        // List<MongoSelector> test = orNode.getNodes().stream().map(node -> {
        // return switch (node) {
        // case AndFilterNode and -> compileAllOfTest(and);
        // default -> compilePropertyTest(node);
        // };
        // }).toList();

        // return null;

        // return new MongoAnyOfSelector(new MongoAllOfSelector(
        // orNode.getNodes().stream()
        // .map(this::compilePropertyTest)
        // .toList()));
    }

    public MongoAlternativesSelector compilePropertyTest(PropertyFilterNode filterNode) {
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

    private MongoAlternativesSelector compilePropertyTest(RegexFilterNode node) {
        var assertion = new RegexMongoValueAssertion(node);
        return compilePropertyTest(node.getRelativeQueryNode(), assertion);
    }

    private MongoAlternativesSelector compilePropertyTest(ExistenceFilterNode node) {
        return compilePropertyTest(node.getRelativeQueryNode(), null);
    }

    private MongoAlternativesSelector compilePropertyTest(RelativeValueComparingNode node) {
        var assertion = new MongoValueComparingAssertion(
                node.getOperator(),
                node.getValueNode().getValue());
        return compilePropertyTest(node.getRelativeQueryNode(), assertion);
    }

    private MongoAlternativesSelector compilePropertyTest(RelativeQueryNode queryNode,
            MongoPropertyAssertion assertion) {
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
