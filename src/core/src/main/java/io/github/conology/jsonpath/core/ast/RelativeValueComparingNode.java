package io.github.conology.jsonpath.core.ast;

public final class RelativeValueComparingNode implements PropertyFilterNode {

    private final RelativeQueryNode queryNode;
    private final ValueNode valueNode;
    private final ComparisonOperator operator;

    public RelativeValueComparingNode(RelativeQueryNode queryNode, ValueNode valueNode, ComparisonOperator operator) {
        this.queryNode = queryNode;
        this.valueNode = valueNode;
        this.operator = operator;
    }

    @Override
    public RelativeQueryNode getRelativeQueryNode() {
        return queryNode;
    }

    public ValueNode getValueNode() {
        return valueNode;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }
}
