package net.conology.restjsonpath.ast;

import java.util.List;

public final class OrFilterNode implements PropertyFilterNode {

    private final List<PropertyFilterNode> list;

    public OrFilterNode(List<PropertyFilterNode> list) {
        this.list = list;
    }

    public List<PropertyFilterNode> getNodes() {
        return list;
    }

}
