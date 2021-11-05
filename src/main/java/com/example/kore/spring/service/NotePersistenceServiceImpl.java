package com.example.kore.spring.service;

import com.example.kore.spring.dao.Note;
import com.example.kore.spring.dao.NoteRepository;
import com.example.kore.spring.exception.IncorrectVersionException;
import com.example.kore.spring.exception.NotSameUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
class NotePersistenceServiceImpl implements NotePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(NotePersistenceServiceImpl.class);

    private final NoteRepository repository;

    NotePersistenceServiceImpl(NoteRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Note saveNote(String username, String content) {
        log.debug("User [{}] is trying to save note with content: {}", username, content);
        var note = new Note(username, content);
        var saved = repository.insert(note);
        log.info(
                "User [{}] created note [{}] at [{}] with version [{}]",
                saved.getUsername(), saved.getId(), saved.getCreatedAt(), saved.getVersion()
        );
        return saved;
    }

    @Override
    @Transactional
    public Optional<Note> updateNote(String username, String content, String id, long version) {
        log.debug("User [{}] is trying to update note [{}]", username, id);

        var found = repository.findById(id);
        if (found.isEmpty()) {
            log.debug("Couldn't find note by id [{}]", id);
            return Optional.empty();
        }

        var note = found.get();

        if (!note.getUsername().equals(username)) {
            throw new NotSameUserException(note.getId(), note.getUsername(), username);
        }

        note.setContent(content);
        note.setVersion(version);

        Note updated;
        try {
            updated = repository.save(note);
        } catch (OptimisticLockingFailureException e) {
            throw new IncorrectVersionException(e, id, username, version);
        }

        log.info(
                "User [{}] updated note [{}] with content [{}] at [{}]",
                username, updated.getId(), updated.getContent(), updated.getLastModifiedAt()
        );

        return Optional.of(updated);
    }
}
