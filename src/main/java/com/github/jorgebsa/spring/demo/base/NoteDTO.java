package com.github.jorgebsa.spring.demo.base;

public record NoteDTO(String id,
                      String username,
                      String content,
                      long createdAt,
                      long lastModifiedAt,
                      long version) {
}
