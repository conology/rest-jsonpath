package net.conology.restjsonpath.ast;

public sealed interface PropertyFilterNode
        extends SelectorNode
        permits AndFilterNode, OrFilterNode, ExistenceFilterNode, RegexFilterNode, RelativeValueComparingNode {
}
