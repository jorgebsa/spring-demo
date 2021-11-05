package com.github.jorgebsa.spring.demo.service;

import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.github.jorgebsa.spring.demo.base.SaveNoteRequest;
import com.github.jorgebsa.spring.demo.base.SaveNoteResponse;
import com.github.jorgebsa.spring.demo.base.UpdateNoteRequest;
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
