package net.conology.restjsonpath.ast;

public sealed interface SelectorNode
    permits FieldSelectorNode, IndexSelectorNode, PropertyFilterNode, SelectorNode.Constant, UnsafeFieldSelector
{
    enum Constant implements SelectorNode {
        WILDCARD
    }
}
