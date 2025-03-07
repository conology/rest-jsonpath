package io.github.conology.jsonpath.core;

public final class ValueNode implements ExpressionNode {
    private final String text;

    public ValueNode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
