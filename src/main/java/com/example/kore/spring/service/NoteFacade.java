package com.example.kore.spring.service;

import com.example.kore.spring.base.NoteDTO;
import com.example.kore.spring.base.SaveNoteRequest;
import com.example.kore.spring.base.SaveNoteResponse;
import com.example.kore.spring.base.UpdateNoteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoteFacade {

    SaveNoteResponse saveNote(SaveNoteRequest request, String username);

    Page<NoteDTO> getPage(Pageable pageable);

    Optional<NoteDTO> findById(String id);

    Optional<NoteDTO> updateNote(UpdateNoteRequest request, String username, String id);

    Optional<NoteDTO> removeNote(String id);

}
