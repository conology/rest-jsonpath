package io.github.conology.jsonpath.core;

import java.util.List;

public non-sealed interface PropertySelector extends ComparableNode {
    String getPath();

    List<String> getPathParts();

    List<FilterNode> getFilters();

    PropertySelector getChildSelector();
}
