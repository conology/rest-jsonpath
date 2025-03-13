package io.github.conology.jsonpath.core.ast;

public sealed interface PropertyFilterNode
    extends SelectorNode
    permits AndFilterNode, ExistenceFilterNode, RegexFilterNode, RelativeValueComparingNode {
}
