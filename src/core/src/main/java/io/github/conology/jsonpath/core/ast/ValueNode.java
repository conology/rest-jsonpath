package io.github.conology.jsonpath.core.ast;

public final class ValueNode implements ComparableNode {
    private final Object value;

    public ValueNode(String value) {
        this.value = value;
    }

    public ValueNode(Integer value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
