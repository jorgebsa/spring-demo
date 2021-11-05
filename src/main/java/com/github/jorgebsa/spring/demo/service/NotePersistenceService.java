package com.github.jorgebsa.spring.demo.service;

import com.github.jorgebsa.spring.demo.dao.Note;

import java.util.Optional;

interface NotePersistenceService {

    Note saveNote(String username, String content);

    Optional<Note> updateNote(String username, String content, String id, long version);
}
