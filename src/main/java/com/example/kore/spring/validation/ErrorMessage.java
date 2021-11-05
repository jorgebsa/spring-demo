package com.example.kore.spring.validation;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.Instant.now;

public record ErrorMessage(String timestamp, int status, Object errors) {

    public ErrorMessage(int status, Object errors) {
        this(ZonedDateTime.ofInstant(now(), ZoneId.systemDefault()).toString(), status, errors);
    }

}