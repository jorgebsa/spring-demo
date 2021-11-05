package com.example.kore.spring.service;

import com.example.kore.spring.base.NoteDTO;
import com.example.kore.spring.base.SaveNoteResponse;
import com.example.kore.spring.dao.Note;
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
