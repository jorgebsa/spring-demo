package com.github.jorgebsa.spring.demo.util;

import com.github.jorgebsa.spring.demo.dao.Note;
import com.github.jorgebsa.spring.demo.dao.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class NoteFactory {

    @Autowired
    private NoteRepository repository;

    public ArrayList<Note> insertNotes(int count) {
        return insertNotes(count, 3);
    }

    public ArrayList<Note> insertNotes(int count, int diffUsers) {
        var savedNotes = new ArrayList<Note>(count);
        for (int i = 0 ; i < count ; i++) {
            var username = "user-" + (i % diffUsers);
            var content = "content of note #" + (i + 1);
            savedNotes.add(new Note(username, content));
        }
        repository.insert(savedNotes);
        return savedNotes;
    }

    public Note insert(String username, String content) {
        return repository.insert(new Note(username, content));
    }

    public Note save(Note note) {
        return repository.save(note);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public Optional<Note> findById(String id) {
        return repository.findById(id);
    }

}
