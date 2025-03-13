package io.github.conology.jsonpath.core.ast;

public sealed interface PropertyFilterNode
    extends SelectorNode
    permits ExistenceFilterNode, RegexFilterNode, RelativeValueComparingNode {
    RelativeQueryNode getRelativeQueryNode();
}
