package io.github.conology.jsonpath.core;

public final class ComparingQuery implements QueryNode {

    private final ComparableNode leftNode;
    private final ComparableNode rightNode;
    private final ComparisonOperator operator;

    public ComparingQuery(ComparableNode leftNode, ComparableNode rightNode, ComparisonOperator operator) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.operator = operator;
    }

    public ComparableNode getLeftNode() {
        return leftNode;
    }

    public ComparableNode getRightNode() {
        return rightNode;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }
}
