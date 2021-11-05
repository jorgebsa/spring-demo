package com.github.jorgebsa.spring.demo.base;

import com.github.jorgebsa.spring.demo.validation.Validatable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

public record UpdateNoteRequest(String content, long version) implements Validatable {

    public UpdateNoteRequest(@NotBlank String content, @Positive long version) {
        validate(content, version);

        this.content = content;
        this.version = version;
    }

}
