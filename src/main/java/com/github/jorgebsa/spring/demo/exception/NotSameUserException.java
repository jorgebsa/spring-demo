package com.github.jorgebsa.spring.demo.exception;

public class NotSameUserException extends RuntimeException {

    private final String noteId;
    private final String noteOwner;
    private final String trespasser;

    public NotSameUserException(String noteId, String noteOwner, String trespasser) {
        this.noteOwner = noteOwner;
        this.trespasser = trespasser;
        this.noteId = noteId;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getNoteOwner() {
        return noteOwner;
    }

    public String getTrespasser() {
        return trespasser;
    }

}
