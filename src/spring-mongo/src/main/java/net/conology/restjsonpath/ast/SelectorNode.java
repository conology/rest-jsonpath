package net.conology.restjsonpath.ast;

public sealed interface SelectorNode
    permits FieldSelectorNode, IndexSelectorNode, PropertyFilterNode, SelectorNode.Constant
{
    enum Constant implements SelectorNode {
        WILDCARD
    }
}
