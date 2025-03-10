package io.github.conology.jsonpath.core;

import java.util.List;

public non-sealed interface PropertyQuery extends ComparableNode {

    List<String> getPath();

    List<FilterNode> getFilters();

    PropertyQuery getChildSelector();
}
