package com.github.jorgebsa.spring.demo.service;

import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.github.jorgebsa.spring.demo.base.SaveNoteResponse;
import com.github.jorgebsa.spring.demo.dao.Note;
import org.springframework.stereotype.Service;

@Service
class NoteMapperImpl implements NoteMapper {

    @Override
    public NoteDTO toDTO(Note note) {
        return new NoteDTO(
                note.getId(),
                note.getUsername(),
                note.getContent(),
                note.getCreatedAt().toEpochMilli(),
                note.getLastModifiedAt().toEpochMilli(),
                note.getVersion()
        );
    }

    @Override
    public SaveNoteResponse toSaveNoteResponse(Note note) {
        return new SaveNoteResponse(
                note.getId(),
                note.getUsername(),
                note.getContent(),
                note.getCreatedAt().toEpochMilli(),
                note.getVersion()
        );
    }

}
