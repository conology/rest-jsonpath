package io.github.conology.jsonpath.mongo.spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoCriteriaCompilerPassTest {

    @ParameterizedTest(name = "{index} {0}")
    @CsvFileSource(resources = "/MongoCriteriaCompilerPassTest.csv", numLinesToSkip = 1)
    void test(String input, String expected) {
        var criteria = MongoCriteriaCompilerPass.parse(input);
        assertEquals(expected, criteria.getCriteriaObject().toJson());
    }

    @Test
    void manualTest2() {
        test(
            "store == \"The Book Haven\"",
            "{\"owner.contacts\": {\"$elemMatch\": {\"type\": \"secondMail\", \"value\": \"alice@bookhaven.com\" }}}"
        );
    }

    @Test
    void manualTest() {
        test(
            "owner.contacts[?@.type==\"secondMail\" && @.value==\"alice@bookhaven.com\"]",
            "{\"owner.contacts\": {\"$elemMatch\": {\"type\": \"secondMail\", \"value\": \"alice@bookhaven.com\" }}}"
        );
    }
}