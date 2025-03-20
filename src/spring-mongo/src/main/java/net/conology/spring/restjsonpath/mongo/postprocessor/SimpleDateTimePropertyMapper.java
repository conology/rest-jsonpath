package net.conology.spring.restjsonpath.mongo.postprocessor;

import net.conology.spring.restjsonpath.mongo.ir.MongoPropertyCondition;

import java.util.*;

public class SimpleDateTimePropertyMapper extends DateTimePropertyMapper {

    private final Set<String> dateTimeFields;

    public SimpleDateTimePropertyMapper(String... dateTimeFields) {
        this(Arrays.asList(dateTimeFields));
    }

    public SimpleDateTimePropertyMapper(List<String> dateTimeFields) {
        this.dateTimeFields = new HashSet<>(dateTimeFields);
    }


    @Override
    protected boolean handles(MongoPropertyCondition test) {
        var fieldName = test.getPropertySelector().getFieldName();
        return dateTimeFields.contains(fieldName);
    }

}
