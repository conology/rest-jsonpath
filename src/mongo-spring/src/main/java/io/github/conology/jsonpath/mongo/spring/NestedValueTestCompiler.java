package io.github.conology.jsonpath.mongo.spring;


import io.github.conology.jsonpath.core.PeekingIterator;
import io.github.conology.jsonpath.core.ast.FieldSelectorNode;
import io.github.conology.jsonpath.core.ast.PropertyFilterNode;
import io.github.conology.jsonpath.core.ast.RelativeQueryNode;
import io.github.conology.jsonpath.core.ast.SelectorNode;
import io.github.conology.jsonpath.mongo.spring.ast.MongoElementMatch;
import io.github.conology.jsonpath.mongo.spring.ast.MongoFieldSelector;
import io.github.conology.jsonpath.mongo.spring.ast.MongoPropertyAssertion;
import io.github.conology.jsonpath.mongo.spring.ast.MongoPropertyTest;

import java.util.LinkedList;

public class NestedValueTestCompiler {

    private final RelativeQueryNode relativeQueryNode;
    private final MongoAstCompilerPass parent;
    private final MongoPropertyAssertion finalAssertion;

    public NestedValueTestCompiler(
        RelativeQueryNode relativeQueryNode,
        MongoAstCompilerPass parent,
        MongoPropertyAssertion finalAssertion
    ) {
        this.relativeQueryNode = relativeQueryNode;
        this.parent = parent;
        this.finalAssertion = finalAssertion;
    }

    public MongoPropertyTest compile() {
        var nodes = PeekingIterator.of(relativeQueryNode.getSelectorNodes().iterator());

        MongoPropertyTest head = null;
        MongoElementMatch tail = null;

        while (nodes.hasNext()) {
            var fieldSelector = compileSelector(nodes);
            var elementMatch = compileElementMatch(nodes);

            if (elementMatch == null && nodes.hasNext()) {
                // field selector should consume everything except for filters that result in elementMatch
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

    private MongoPropertyTest createTest(MongoFieldSelector fieldSelector, MongoElementMatch elementMatch) {
        var assertion =
            elementMatch != null ? elementMatch
            : ( finalAssertion != null ? finalAssertion
                : parent.getExistenceAssertion()
            );
        return new MongoPropertyTest(
            fieldSelector,
            assertion
        );
    }

    private MongoFieldSelector compileSelector(PeekingIterator<SelectorNode> nodes) {
        var path = new LinkedList<String>();

        while (nodes.hasNext()) {
            var next = nodes.peek();
            if (next instanceof FieldSelectorNode fieldSelectorNode) {
                nodes.next();
                path.addAll(fieldSelectorNode.getPath());
            } else {
                break;
            }
        }

        return new MongoFieldSelector(parent.normalizePath(path));
    }

    private MongoElementMatch compileElementMatch(
        PeekingIterator<SelectorNode> nodes
    ) {
        var propertyTests = new LinkedList<MongoPropertyTest>();
        while (nodes.hasNext()) {
            var next = nodes.peek();
            if (next instanceof PropertyFilterNode filterNode) {
                nodes.next();
                propertyTests.add(parent.compile(filterNode));
            } else {
                break;
            }
        }
        return propertyTests.isEmpty() ? null
            : new MongoElementMatch(propertyTests);
    }
}
