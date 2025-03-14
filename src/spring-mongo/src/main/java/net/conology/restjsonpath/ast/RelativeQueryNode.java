package net.conology.restjsonpath.ast;

import java.util.LinkedList;
import java.util.List;

public final class RelativeQueryNode
    implements ComparableNode
{

    private final List<SelectorNode> selectorNodes = new LinkedList<>();

    public List<SelectorNode> getSelectorNodes() {
        return selectorNodes;
    }

    public void addNode(SelectorNode selectorNode) {
        selectorNodes.add(selectorNode);
    }
}
