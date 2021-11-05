package com.github.jorgebsa.spring.demo.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViolationTest {

    @Test
    void testAsMaps() {
        var expected = "Violations can't be null";
        assertAll(
                () -> {
                    var e = assertThrows(IllegalArgumentException.class, () -> Violation.asMaps((Violation[]) null), "Varargs...");
                    assertThat(e.getMessage()).isEqualTo(expected);
                },
                () -> {
                    var e = assertThrows(IllegalArgumentException.class, () -> Violation.asMaps((List<Violation>) null), "List<>");
                    assertThat(e.getMessage()).isEqualTo(expected);
                }
        );
    }
}