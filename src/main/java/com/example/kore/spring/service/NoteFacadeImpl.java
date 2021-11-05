package com.example.kore.spring.service;

import com.example.kore.spring.base.NoteDTO;
import com.example.kore.spring.base.SaveNoteRequest;
import com.example.kore.spring.base.SaveNoteResponse;
import com.example.kore.spring.base.UpdateNoteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NoteFacadeImpl implements NoteFacade {

    private final NotePersistenceService persistenceService;
    private final NoteRetrievalService retrievalService;
    private final NoteRemovalService removalService;
    private final NoteMapper mapper;

    public NoteFacadeImpl(NotePersistenceService persistenceService, NoteRetrievalService retrievalService, NoteRemovalService removalService, NoteMapper mapper) {
        this.persistenceService = persistenceService;
        this.retrievalService = retrievalService;
        this.removalService = removalService;
        this.mapper = mapper;
    }

    @Override
    public SaveNoteResponse saveNote(SaveNoteRequest request, String username) {
        var saved = persistenceService.saveNote(username, request.content());
        return mapper.toSaveNoteResponse(saved);
    }

    @Override
    public Page<NoteDTO> getPage(Pageable pageable) {
        var page = retrievalService.getPage(pageable);
        return page.map(mapper::toDTO);
    }

    @Override
    public Optional<NoteDTO> findById(String id) {
        var found = retrievalService.findById(id);
        return found.map(mapper::toDTO);
    }

    @Override
    public Optional<NoteDTO> updateNote(UpdateNoteRequest request, String username, String id) {
        var updated = persistenceService.updateNote(username, request.content(), id, request.version());
        return updated.map(mapper::toDTO);
    }

    @Override
    public Optional<NoteDTO> removeNote(String id) {
        var removed = removalService.remove(id);
        return removed.map(mapper::toDTO);
    }
}
