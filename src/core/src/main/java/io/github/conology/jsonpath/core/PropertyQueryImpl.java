package io.github.conology.jsonpath.core;

import java.util.ArrayList;
import java.util.List;

public final class PropertyQueryImpl implements PropertyQuery {

    private final List<String> path = new ArrayList<>();
    private final List<FilterNode> filters = new ArrayList<>();
    private PropertyQuery childSelector;

    @Override
    public List<String> getPath() {
        return path;
    }

    @Override
    public List<FilterNode> getFilters() {
        return filters;
    }

    public void addFilter(FilterNode filterNode) {
        filters.add(filterNode);
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
