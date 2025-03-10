package io.github.conology.jsonpath.core;

import java.util.ArrayList;
import java.util.List;

public final class PropertySelectorImpl implements PropertySelector {

    private final List<String> pathParts = new ArrayList<>();
    private final List<FilterNode> filters = new ArrayList<>();
    private PropertySelector childSelector;

    @Override
    public String getPath() {
        return String.join(".", pathParts);
    }

    @Override
    public List<String> getPathParts() {
        return pathParts;
    }

    @Override
    public List<FilterNode> getFilters() {
        return filters;
    }

    public void addFilter(FilterNode filterNode) {
        filters.add(filterNode);
    }

    public void appendField(String field) {
        pathParts.add(field);
    }


    @Override
    public PropertySelector getChildSelector() {
        return childSelector;
    }

    public void setChildSelector(PropertySelector childSelector) {
        this.childSelector = childSelector;
    }
}
