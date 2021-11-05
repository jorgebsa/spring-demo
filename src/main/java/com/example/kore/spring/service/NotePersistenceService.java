package com.example.kore.spring.service;

import com.example.kore.spring.dao.Note;

import java.util.Optional;

interface NotePersistenceService {

    Note saveNote(String username, String content);

    Optional<Note> updateNote(String username, String content, String id, long version);
}
