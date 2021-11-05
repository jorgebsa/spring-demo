package com.example.kore.spring.base;

public record SaveNoteResponse(String id,
                               String username,
                               String content,
                               long createdAt,
                               long version) {

}
