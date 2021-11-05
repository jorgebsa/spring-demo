package com.github.jorgebsa.spring.demo.validation;

import javax.validation.Validation;
import javax.validation.Validator;

public class ValidatorProvider {

    private static final Validator VALIDATOR;

    static {
        VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public static Validator getValidator() {
        return VALIDATOR;
    }

    private ValidatorProvider() {

    }

}
