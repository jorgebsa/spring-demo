package com.github.jorgebsa.spring.demo.service;

import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.github.jorgebsa.spring.demo.base.SaveNoteResponse;
import com.github.jorgebsa.spring.demo.dao.Note;

public interface NoteMapper {

    NoteDTO toDTO(Note note);

    SaveNoteResponse toSaveNoteResponse(Note note);
}
