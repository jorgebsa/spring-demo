package com.example.kore.spring.service;

import com.example.kore.spring.dao.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

interface NoteRetrievalService {

    Page<Note> getPage(Pageable pageable);

    Optional<Note> findById(String id);
}
