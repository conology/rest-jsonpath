package net.conology.spring.restjsonpath.mongo.postprocessor;

import net.conology.restjsonpath.ast.ComparisonOperator;
import net.conology.spring.restjsonpath.mongo.ir.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleFieldNameMapperTest {

    @Test
    void itMapsFieldNames() {
        var allOfSelector = new MongoAllOfSelector(
            List.of(new MongoPropertyCondition(
                new MongoFieldSelector(List.of("id")),
                    new MongoValueComparingAssertion(ComparisonOperator.EQ, "test2")
            ))
        );

        var query = new MongoAllOfSelector(
            List.of(
                new MongoPropertyCondition(
                    new MongoFieldSelector(List.of("store", "id")),
                    new MongoValueComparingAssertion(ComparisonOperator.EQ, "test")
                ),
                new MongoPropertyCondition(
                    new MongoFieldSelector(List.of("store", "books")),
                    new MongoElementMatch(allOfSelector)
                )
            )
        );

        var simpleFieldNameMapper = new SimpleFieldNameMapper(Map.of("id", "_id"));

        simpleFieldNameMapper.accept(query);

        var firstFieldName = ((MongoPropertyCondition) query.getTests().getFirst()).getPropertySelector().getFieldName();
        var secondFieldName = ((MongoPropertyCondition) (
            (MongoElementMatch)(
                (MongoPropertyCondition) query.getTests().getLast()).getAssertion()
            ).getTests().getTests().getFirst()
        ).getPropertySelector().getFieldName();

        assertThat(firstFieldName).isEqualTo("_id");
        assertThat(secondFieldName).isEqualTo("_id");

    }
}
