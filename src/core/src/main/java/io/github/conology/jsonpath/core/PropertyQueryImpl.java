package io.github.conology.jsonpath.core;

import java.util.ArrayList;
import java.util.List;

public final class PropertyQueryImpl implements PropertyQuery {

    private final List<String> path = new ArrayList<>();
    private final List<QueryNode> filters = new ArrayList<>();
    private PropertyQuery childSelector;

    @Override
    public List<String> getPath() {
        return path;
    }

    @Override
    public List<QueryNode> getFilters() {
        return filters;
    }

    public void addFilter(QueryNode queryNode) {
        filters.add(queryNode);
    }

    public void appendField(String field) {
        path.add(field);
    }


    @Override
    public PropertyQuery getChildSelector() {
        return childSelector;
    }

    public void setChildSelector(PropertyQuery childSelector) {
        this.childSelector = childSelector;
    }
}
