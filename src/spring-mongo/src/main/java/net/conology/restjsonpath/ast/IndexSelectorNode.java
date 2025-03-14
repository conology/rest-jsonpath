package net.conology.restjsonpath.ast;

public final class IndexSelectorNode implements SelectorNode {
    private final int index;

    public IndexSelectorNode(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
