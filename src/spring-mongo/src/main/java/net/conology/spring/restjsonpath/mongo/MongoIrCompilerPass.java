package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.ast.*;
import net.conology.spring.restjsonpath.mongo.ir.*;

import java.util.List;

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
            andNode
                .getNodes()
                .stream()
                .map(node -> {
                    return switch (node) {
                        case RelativeValueComparingNode comparingFilter -> compilePropertyTest(comparingFilter);
                        case ExistenceFilterNode existenceFilter -> compilePropertyTest(existenceFilter);
                        case RegexFilterNode regexFilterNode -> compilePropertyTest(regexFilterNode);
                        case OrFilterNode orFilterNode -> compileAnyOfTest(orFilterNode);
                        case AndFilterNode andFilterNode -> compileAllOfTest(andFilterNode);
                    };
                })
                .toList()
        );
    }

    private MongoAnyOfSelector compileAnyOfTest(OrFilterNode orNode) {
        return new MongoAnyOfSelector(
            orNode
                .getNodes()
                .stream()
                .map(node -> {
                    return switch (node) {
                        case RelativeValueComparingNode comparingFilter ->
                            new MongoAllOfSelector(List.of(compilePropertyTest(comparingFilter)));
                        case ExistenceFilterNode existenceFilter ->
                            new MongoAllOfSelector(List.of(compilePropertyTest(existenceFilter)));
                        case RegexFilterNode regexFilterNode ->
                            new MongoAllOfSelector(List.of(compilePropertyTest(regexFilterNode)));
                        case OrFilterNode orFilterNode ->
                            new MongoAllOfSelector(List.of(compileAnyOfTest(orFilterNode)));
                        case AndFilterNode ignored ->
                            compileAllOfTest(ignored);
                    };
                })
                .toList()
        );
    }

    public MongoAlternativesSelector compilePropertyTest(
        PropertyFilterNode filterNode
    ) {
        return switch (filterNode) {
            case RelativeValueComparingNode comparingFilter -> compilePropertyTest(comparingFilter);
            case ExistenceFilterNode existenceFilter -> compilePropertyTest(existenceFilter);
            case RegexFilterNode regexFilterNode -> compilePropertyTest(regexFilterNode);
            case OrFilterNode ignored ->
                throw new RuntimeException("OrFilterNode not expected here. This should never happen.");
            case AndFilterNode ignored ->
                throw new RuntimeException("AndFilterNode not expected here. This should never happen.");
        };
    }

    private MongoAlternativesSelector compilePropertyTest(
        RegexFilterNode node
    ) {
        var assertion = new RegexMongoValueAssertion(node);
        return compilePropertyTest(node.getRelativeQueryNode(), assertion);
    }

    private MongoAlternativesSelector compilePropertyTest(
        ExistenceFilterNode node
    ) {
        return compilePropertyTest(node.getRelativeQueryNode(), null);
    }

    private MongoAlternativesSelector compilePropertyTest(
        RelativeValueComparingNode node
    ) {
        var assertion = new MongoValueComparingAssertion(
            node.getOperator(),
            node.getValueNode().getValue()
        );
        return compilePropertyTest(node.getRelativeQueryNode(), assertion);
    }

    private MongoAlternativesSelector compilePropertyTest(
        RelativeQueryNode queryNode,
        MongoPropertyAssertion assertion
    ) {
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
                existenceAssertion != null
                    ? existenceAssertion
                    : MongoDelegatingValueAssertion.createDefaultExistenceAssertion()
            );
        }

        public Builder existenceAssertion(
            MongoValueAssertion existenceAssertion
        ) {
            this.existenceAssertion = existenceAssertion;
            return this;
        }
    }
}
