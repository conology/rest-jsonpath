package net.conology.restjsonpath.ast;

import java.util.ArrayList;
import java.util.List;

public final class RelativeQueryNode
    implements ComparableNode
{

    private final List<SelectorNode> selectorNodes = new ArrayList<>();

    public List<SelectorNode> getSelectorNodes() {
        return selectorNodes;
    }

    public void addNode(SelectorNode selectorNode) {
        selectorNodes.add(selectorNode);
    }
}
