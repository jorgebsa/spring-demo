package com.example.kore.spring.base;

public record NoteDTO(String id,
                      String username,
                      String content,
                      long createdAt,
                      long lastModifiedAt,
                      long version) {
}
