package com.github.jorgebsa.spring.demo.validation;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.stream.Collectors;

public interface Validatable {

    @SuppressWarnings({"unchecked", "rawtypes"})
    default void validate(Object... args) {
        var validator = ValidatorProvider.getValidator();
        Constructor constructor = getClass().getDeclaredConstructors()[0];
        Set<ConstraintViolation<?>> violations = validator.forExecutables()
                .validateConstructorParameters(constructor, args);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(System.lineSeparator()));
            throw new ConstraintViolationException(message, violations);
        }
    }

}
