package io.github.goatfryed.json_path_mongo.parser;

public final class ValueNode implements ExpressionNode {
    private final String text;

    public ValueNode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
