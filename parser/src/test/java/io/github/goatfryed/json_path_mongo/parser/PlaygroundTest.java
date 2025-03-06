package io.github.goatfryed.json_path_mongo.parser;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaygroundTest {

    @Test
    void convertSimple() {
        var out = TestCompilerPass.parse("phoneNumbers");
        assertEquals("phoneNumbers", out);
    }

    @Test
    void convertRootQualifier() {
        var out = TestCompilerPass.parse("$.phoneNumbers");
        assertEquals("phoneNumbers", out);
    }

    @Test
    void convertWithPath() {
        var out = TestCompilerPass.parse("phoneNumbers.type");
        assertEquals("phoneNumbers->type", out);
    }

    @Test
    void convertWithSimpleFilter() {
        var out = TestCompilerPass.parse("relationship[?@.type]");
        assertEquals("relationship[having:self->type]->contact", out);
    }

    @Test
    void convertWithFilter() {
        var out = TestCompilerPass.parse("relationship[@.type].contact");
        assertEquals("relationship[having:self->type]->contact", out);
    }

    @Test
    @Disabled
    void convertBrackets() {
        var out = TestCompilerPass.parse("[\"phoneNumbers\"]");
        assertEquals("phoneNumbers", out);
    }

    @Test
    @Disabled
    void convertGoal() {
        var out = TestCompilerPass.parse("phoneNumbers[?@.type == \"iPhone\" && @.number == \"0123-4567-8888\"]");

        assertEquals("test", out);
    }
}