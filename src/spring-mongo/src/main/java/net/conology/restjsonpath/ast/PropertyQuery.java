package net.conology.restjsonpath.ast;

import java.util.List;

public interface PropertyQuery {

    List<String> getPath();

    List<PropertyFilterNode> getFilters();

    PropertyQuery getChildSelector();
}
