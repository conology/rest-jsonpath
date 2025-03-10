package io.github.conology.jsonpath.core;

import java.util.List;

public non-sealed interface PropertyQuery extends ComparableNode, QueryNode {

    List<String> getPath();

    List<QueryNode> getFilters();

    PropertyQuery getChildSelector();
}
