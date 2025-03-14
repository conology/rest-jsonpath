package net.conology.restjsonpath.ast;

import java.util.List;

public final class AndFilterNode implements PropertyFilterNode {

    private final List<PropertyFilterNode> list;

    public AndFilterNode(List<PropertyFilterNode> list) {
        this.list = list;
    }

    public List<PropertyFilterNode> getNodes() {
        return list;
    }
}
