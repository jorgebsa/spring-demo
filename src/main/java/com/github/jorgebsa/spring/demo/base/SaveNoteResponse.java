package com.github.jorgebsa.spring.demo.base;

public record SaveNoteResponse(String id,
                               String username,
                               String content,
                               long createdAt,
                               long version) {

}
