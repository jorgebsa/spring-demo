package com.github.jorgebsa.spring.demo.base;

import com.github.jorgebsa.spring.demo.validation.Validatable;

import javax.validation.constraints.NotBlank;

public record SaveNoteRequest(String content) implements Validatable {

    public SaveNoteRequest(@NotBlank String content) {
        validate(content);
        this.content = content;
    }

}
