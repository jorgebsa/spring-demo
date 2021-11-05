package com.example.kore.spring.service;

import com.example.kore.spring.base.NoteDTO;
import com.example.kore.spring.base.SaveNoteResponse;
import com.example.kore.spring.dao.Note;

public interface NoteMapper {

    NoteDTO toDTO(Note note);

    SaveNoteResponse toSaveNoteResponse(Note note);
}
