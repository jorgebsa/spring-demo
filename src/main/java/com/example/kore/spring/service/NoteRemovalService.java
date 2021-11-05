package com.example.kore.spring.service;

import com.example.kore.spring.dao.Note;

import java.util.Optional;

interface NoteRemovalService {

    Optional<Note> remove(String id);
}
