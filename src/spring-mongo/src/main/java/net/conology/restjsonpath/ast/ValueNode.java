package net.conology.restjsonpath.ast;

public final class ValueNode implements ComparableNode {
    private final Object value;

    public ValueNode(String value) {
        this.value = value;
    }

    public ValueNode(Integer value) {
        this.value = value;
    }

    public ValueNode(boolean value) {
        this.value = value;
    }

    public ValueNode(SPECIAL_VALUE value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public enum SPECIAL_VALUE {
        NULL("null"),
        ;

        private final String symbol;

        SPECIAL_VALUE(String symbol) {
            this.symbol = symbol;
        }
    }
}
