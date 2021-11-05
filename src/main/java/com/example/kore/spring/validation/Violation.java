package com.example.kore.spring.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.validation.FieldError;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public record Violation(String field, String message) {

    public static final Comparator<Violation> COMPARATOR = Comparator.comparing(Violation::field)
            .thenComparing(Violation::message);

    @JsonCreator
    public Violation {

    }

    public Violation(FieldError fieldError) {
        this(fieldError.getField(), fieldError.getDefaultMessage());
    }

    @JsonIgnore
    public Map<String, String> asMap() {
        return Map.of("field", field, "message", message);
    }

    public static List<Map<String, String>> asMaps(List<Violation> violations) {
        if (violations == null) {
            throw new IllegalArgumentException("Violations can't be null");
        }
        return asMaps(violations.stream());
    }

    public static List<Map<String, String>> asMaps(Violation... violations) {
        if (violations == null) {
            throw new IllegalArgumentException("Violations can't be null");
        }
        return asMaps(Stream.of(violations));
    }

    private static List<Map<String, String>> asMaps(Stream<Violation> violations) {
        return violations.sorted(COMPARATOR)
                .map(Violation::asMap)
                .collect(toList());
    }

}