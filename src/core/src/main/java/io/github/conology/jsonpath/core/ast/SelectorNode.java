package io.github.conology.jsonpath.core.ast;

public sealed interface SelectorNode
    permits FieldSelectorNode, IndexSelectorNode, PropertyFilterNode, SelectorNode.Constant
{
    enum Constant implements SelectorNode {
        WILDCARD
    }
}
