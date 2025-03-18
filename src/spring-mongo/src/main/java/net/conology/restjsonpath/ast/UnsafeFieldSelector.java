package net.conology.restjsonpath.ast;

public final class UnsafeFieldSelector implements SelectorNode {
    private final String fieldName;

    public UnsafeFieldSelector(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
