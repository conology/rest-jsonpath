package net.conology.spring.restjsonpath.mongo;

import java.util.ArrayList;
import net.conology.restjsonpath.PeekingIterator;
import net.conology.restjsonpath.ast.*;
import net.conology.spring.restjsonpath.mongo.ir.*;

public class NestedValueTestCompiler {

    private final RelativeQueryNode relativeQueryNode;
    private final MongoIrCompilerPass parent;
    private final MongoPropertyAssertion finalAssertion;

    public NestedValueTestCompiler(
        RelativeQueryNode relativeQueryNode,
        MongoIrCompilerPass parent,
        MongoPropertyAssertion finalAssertion
    ) {
        this.relativeQueryNode = relativeQueryNode;
        this.parent = parent;
        this.finalAssertion = finalAssertion;
    }

    public MongoPropertyCondition compile() {
        var nodes = PeekingIterator.of(
            relativeQueryNode.getSelectorNodes().iterator()
        );

        MongoPropertyCondition head = null;
        MongoElementMatch tail = null;

        while (nodes.hasNext()) {
            var fieldSelector = compileSelector(nodes);
            var elementMatch = compileElementMatch(nodes);

            if (elementMatch == null && nodes.hasNext()) {
                // field selector should consume everything except for filters that result in
                // elementMatch
                throw new AssertionError(
                    "illegal state. this indicates a mismatch between parser and compiler"
                );
            }

            var currentTest = createTest(fieldSelector, elementMatch);
            if (head == null) {
                head = currentTest;
            }
            if (tail != null) {
                tail.addTest(currentTest);
            }
            tail = elementMatch;
        }

        return head;
    }

    private MongoPropertyCondition createTest(
        MongoFieldSelector fieldSelector,
        MongoElementMatch elementMatch
    ) {
        var assertion = elementMatch != null
            ? elementMatch
            : (finalAssertion != null
                    ? finalAssertion
                    : parent.getExistenceAssertion());
        return new MongoPropertyCondition(fieldSelector, assertion);
    }

    private MongoFieldSelector compileSelector(
        PeekingIterator<SelectorNode> nodes
    ) {
        var path = new ArrayList<String>();

        while (nodes.hasNext()) {
            var next = nodes.peek();
            var handled =
                switch (next) {
                    case SelectorNode.Constant.WILDCARD -> true;
                    case IndexSelectorNode indexSelectorNode -> {
                        path.add(
                            Integer.toString(indexSelectorNode.getIndex())
                        );
                        yield true;
                    }
                    case UnsafeFieldSelector it -> {
                        path.add(it.getFieldName());
                        yield true;
                    }
                    case FieldSelectorNode fieldSelectorNode -> {
                        path.addAll(fieldSelectorNode.getPath());
                        yield true;
                    }
                    case PropertyFilterNode ignored -> false;
                };
            if (handled) {
                nodes.next();
            } else {
                break;
            }
        }

        return new MongoFieldSelector(path);
    }

    private MongoElementMatch compileElementMatch(
        PeekingIterator<SelectorNode> nodes
    ) {
        var alternativesSelectors = new ArrayList<MongoAlternativesSelector>();
        while (nodes.hasNext()) {
            var next = nodes.peek();
            if (next instanceof PropertyFilterNode filterNode) {
                nodes.next();
                var testNode = parent.compileTestNode(filterNode);
                switch (testNode) {
                    case MongoAllOfSelector allOf -> alternativesSelectors.addAll(
                        allOf.getTests()
                    );
                    case MongoAnyOfSelector anyOf -> alternativesSelectors.add(
                        anyOf
                    );
                    case MongoPropertyCondition propertyTest -> alternativesSelectors.add(
                        propertyTest
                    );
                }
            } else {
                break;
            }
        }
        return alternativesSelectors.isEmpty()
            ? null
            : new MongoElementMatch(
                new MongoAllOfSelector(alternativesSelectors)
            );
    }
}
