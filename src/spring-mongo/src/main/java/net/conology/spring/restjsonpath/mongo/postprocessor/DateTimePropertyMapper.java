package net.conology.spring.restjsonpath.mongo.postprocessor;

import net.conology.spring.restjsonpath.mongo.PropertyTestConfigurer;
import net.conology.spring.restjsonpath.mongo.ir.MongoValueComparingAssertion;

import java.time.Instant;
import java.util.Date;

public abstract class DateTimePropertyMapper extends PropertyTestConfigurer {
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
