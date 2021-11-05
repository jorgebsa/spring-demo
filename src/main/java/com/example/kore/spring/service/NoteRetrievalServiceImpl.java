package com.example.kore.spring.service;

import com.example.kore.spring.dao.Note;
import com.example.kore.spring.dao.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class NoteRetrievalServiceImpl implements NoteRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(NoteRetrievalServiceImpl.class);

    private final NoteRepository repository;

    NoteRetrievalServiceImpl(NoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Note> getPage(Pageable pageable) {
        log.debug("Finding page of Notes with: {}", pageable);
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Note> findById(String id) {
        log.debug("Finding note by id [{}]", id);
        return repository.findById(id);
    }

}
