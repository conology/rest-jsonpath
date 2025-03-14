package net.conology.restjsonpath.ast;

import java.util.Set;

public final class RegexFilterNode implements PropertyFilterNode {

    private final RelativeQueryNode relativeQueryNode;
    private final String regexPattern;
    private final Set<Character> options;
    private final boolean isNegated;

    public RegexFilterNode(
        RelativeQueryNode relativeQueryNode,
        String regexPattern,
        Set<Character> options,
        boolean isNegated
    ) {
        this.relativeQueryNode = relativeQueryNode;
        this.regexPattern = regexPattern;
        this.options = options;
        this.isNegated = isNegated;
    }

    public RelativeQueryNode getRelativeQueryNode() {
        return relativeQueryNode;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public Set<Character> getOptions() {
        return options;
    }

    public boolean isNegated() {
        return isNegated;
    }
}
