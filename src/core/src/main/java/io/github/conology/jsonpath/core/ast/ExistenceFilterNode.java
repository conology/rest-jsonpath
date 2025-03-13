package io.github.conology.jsonpath.core.ast;

public final class ExistenceFilterNode implements PropertyFilterNode {

    private final RelativeQueryNode relativeQueryNode;

    public ExistenceFilterNode(RelativeQueryNode relativeQueryNode) {
        this.relativeQueryNode = relativeQueryNode;
    }

    @Override
    public RelativeQueryNode getRelativeQueryNode() {
        return relativeQueryNode;
    }
}
