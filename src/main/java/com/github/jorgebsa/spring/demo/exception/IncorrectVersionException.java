package com.github.jorgebsa.spring.demo.exception;

public class IncorrectVersionException extends RuntimeException {

    private final String noteId;
    private final String username;
    private final long version;

    public IncorrectVersionException(Throwable cause, String noteId, String username, long version) {
        super(cause);
        this.noteId = noteId;
        this.username = username;
        this.version = version;
    }

    public String getNoteId() {
        return noteId;
    }

    public long getVersion() {
        return version;
    }

    public String getUsername() {
        return username;
    }

}
