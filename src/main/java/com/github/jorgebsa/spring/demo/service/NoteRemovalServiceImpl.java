package com.github.jorgebsa.spring.demo.service;

import com.github.jorgebsa.spring.demo.dao.Note;
import com.github.jorgebsa.spring.demo.dao.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class NoteRemovalServiceImpl implements NoteRemovalService {

    private static final Logger log = LoggerFactory.getLogger(NoteRemovalServiceImpl.class);

    private final NoteRepository repository;

    NoteRemovalServiceImpl(NoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Note> remove(String id) {
        log.info("REMOVING NOTE BY ID: [{}]", id);
        var optional = repository.findById(id);
        if (optional.isEmpty()) {
            log.info("Could not find note by id [{}] in order to remove it", id);
            return Optional.empty();
        }
        var note = optional.get();
        log.debug("FOUND NOTE [{}]", note.getId());
        repository.delete(note);
        log.info("REMOVED NOTE BY ID [{}]", note.getId());
        return Optional.of(note);
    }
}
