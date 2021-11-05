package com.example.kore.spring.base;

import com.example.kore.spring.validation.Validatable;

import javax.validation.constraints.NotBlank;

public record SaveNoteRequest(String content) implements Validatable {

    public SaveNoteRequest(@NotBlank String content) {
        validate(content);
        this.content = content;
    }

}
