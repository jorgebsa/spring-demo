package com.github.jorgebsa.spring.demo.service;

import com.github.jorgebsa.spring.demo.dao.Note;

import java.util.Optional;

interface NoteRemovalService {

    Optional<Note> remove(String id);
}
