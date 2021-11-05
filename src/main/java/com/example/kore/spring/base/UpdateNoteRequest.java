package com.example.kore.spring.base;

import com.example.kore.spring.validation.Validatable;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public record UpdateNoteRequest(String content, long version) implements Validatable {

    public UpdateNoteRequest(@NotBlank String content, @Min(0) long version) {
        validate(content, version);

        this.content = content;
        this.version = version;
    }

}
