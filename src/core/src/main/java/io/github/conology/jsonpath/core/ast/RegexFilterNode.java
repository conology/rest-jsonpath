package io.github.conology.jsonpath.core.ast;

import java.util.Set;

public final class RegexFilterNode implements PropertyFilterNode {

    private final RelativeQueryNode relativeQueryNode;
    private final String regexPattern;
    private final Set<Character> options;

    public RegexFilterNode(
        RelativeQueryNode relativeQueryNode,
        String regexPattern,
        Set<Character> options
    ) {
        this.relativeQueryNode = relativeQueryNode;
        this.regexPattern = regexPattern;
        this.options = options;
    }

    @Override
    public RelativeQueryNode getRelativeQueryNode() {
        return relativeQueryNode;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public Set<Character> getOptions() {
        return options;
    }
}
