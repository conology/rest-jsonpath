package io.github.goatfryed.json_path_mongo.spring_mongo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoCriteriaCompilerPassTest {

    @ParameterizedTest(name = "{index} {0}")
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void test(String input, String expected) {
        var criteria = MongoCriteriaCompilerPass.parse(input);
        assertEquals(expected, criteria.getCriteriaObject().toJson());
    }
}