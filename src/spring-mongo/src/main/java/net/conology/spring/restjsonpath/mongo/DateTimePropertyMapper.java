package net.conology.spring.restjsonpath.mongo;

import com.mongodb.BasicDBObject;
import net.conology.spring.restjsonpath.mongo.ast.MongoPropertyTest;
import net.conology.spring.restjsonpath.mongo.ast.MongoValueComparingAssertion;
import org.bson.Document;

import java.time.Instant;
import java.util.*;

public class DateTimePropertyMapper extends PropertyTestConfigurer {

    private final Set<String> dateTimeFields;

    public DateTimePropertyMapper(String... dateTimeFields) {
        this(Arrays.asList(dateTimeFields));
    }

    public DateTimePropertyMapper(List<String> dateTimeFields) {
        this.dateTimeFields = new HashSet<>(dateTimeFields);
    }


    @Override
    protected boolean isHandledField(MongoPropertyTest test) {
        var fieldName = test.getPropertySelector().getFieldName();
        if (!dateTimeFields.contains(fieldName)) {
            return true;
        }
        return false;
    }

    @Override
    protected void accept(MongoValueComparingAssertion valueComparison) {
        valueComparison.updateValue(this::toDate);
    }

    protected Object toDate(Object object) {
        if (object instanceof String dateTimeString) {
            return toDate(dateTimeString);
        }
        return object;
    }

    protected Object toDate(String dateTimeString) {
        try {
            var instant = Instant.parse(dateTimeString);
            return Date.from(instant);
        } catch (Exception cause) {
            throw new IllegalArgumentException(
                "invalid datetime string on datetime field",
                cause
            );
        }
    }
}
