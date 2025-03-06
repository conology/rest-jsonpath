package io.github.goatfryed.json_path_mongo.parser;

public final class PropertyFilter implements CriteriaNode {
    private final PropertySelector propertyNode;
    private final ValueNode valueNode;
    private final ComparisonOperator operator;

    public PropertyFilter(PropertySelector leftNode, ValueNode valueNode, ComparisonOperator operator) {
        this.propertyNode = leftNode;
        this.valueNode = valueNode;
        this.operator = operator;
    }

    public PropertySelector getPropertyNode() {
        return propertyNode;
    }

    public ValueNode getValueNode() {
        return valueNode;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }
}
