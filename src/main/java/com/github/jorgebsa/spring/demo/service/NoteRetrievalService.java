package com.github.jorgebsa.spring.demo.service;

import com.github.jorgebsa.spring.demo.dao.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

interface NoteRetrievalService {

    Page<Note> getPage(Pageable pageable);

    Optional<Note> findById(String id);
}
