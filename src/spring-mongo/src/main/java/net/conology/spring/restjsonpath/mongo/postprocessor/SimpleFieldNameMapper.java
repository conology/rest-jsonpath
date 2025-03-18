package net.conology.spring.restjsonpath.mongo.postprocessor;

import net.conology.spring.restjsonpath.mongo.ir.MongoPropertyCondition;

import java.util.Collections;
import java.util.Map;

public class SimpleFieldNameMapper extends AbstractMongoTestPostProcessor {

    private final Map<String,String> mappings;

    public SimpleFieldNameMapper(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public SimpleFieldNameMapper(String external, String internal) {
        this(Collections.singletonMap(external, internal));
    }

    @Override
    public void accept(MongoPropertyCondition test) {
        var propertySelector = test.getPropertySelector();
        var path = propertySelector.getPath();
        var mappedPath = path.stream()
            .map(fieldName -> mappings.getOrDefault(fieldName, fieldName))
            .toList();
        propertySelector.setPath(mappedPath);
    }
}
