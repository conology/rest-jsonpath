package io.github.conology.jsonpath.core.ast;

import java.util.List;

public final class FieldSelectorNode implements SelectorNode {

    private final List<String> path;
    private RelativeQueryNode context;

    public FieldSelectorNode(List<String> path, RelativeQueryNode relativeQuery) {
        this.path = path;
    }

    public List<String> getPath() {
        return path;
    }

    public RelativeQueryNode getContext() {
        return context;
    }

    public void setContext(RelativeQueryNode context) {
        this.context = context;
    }
}
