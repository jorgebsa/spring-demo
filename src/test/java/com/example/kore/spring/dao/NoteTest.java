package com.example.kore.spring.dao;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class NoteTest {

    @Test
    void setId() {
        var note = new Note();
        assertThat(note.getId()).isNull();
        note.setId("yo");
        assertThat(note.getId()).isEqualTo("yo");
    }

    @Test
    void setUsername() {
        var note = new Note();
        assertThat(note.getUsername()).isNull();
        note.setUsername("yo");
        assertThat(note.getUsername()).isEqualTo("yo");
    }

    @Test
    void setCreatedAt() {
        var note = new Note();
        assertThat(note.getCreatedAt()).isNull();
        var now = Instant.now();
        note.setCreatedAt(now);
        assertThat(note.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setLastModifiedAt() {
        var note = new Note();
        assertThat(note.getLastModifiedAt()).isNull();
        var now = Instant.now();
        note.setLastModifiedAt(now);
        assertThat(note.getLastModifiedAt()).isEqualTo(now);
    }

    @Test
    void setVersion() {
        var note = new Note();
        assertThat(note.getVersion()).isEqualTo(0L);
        note.setVersion(123L);
        assertThat(note.getVersion()).isEqualTo(123L);
    }

    @Test
    void setContent() {
        var note = new Note();
        assertThat(note.getContent()).isNull();
        note.setContent("yo");
        assertThat(note.getContent()).isEqualTo("yo");
    }

    @Test
    void testEquals() {
        assertAll(
                () -> {
                    var note = new Note("yay");
                    assertThat(note.equals(note)).isTrue();
                },
                () -> {
                    var note = new Note("yo");
                    assertThat(note.equals("yo")).isFalse();
                },
                () -> {
                    var note = new Note("yo");
                    var note2 = new Note("yo");
                    assertThat(note).isEqualTo(note2);
                },
                () -> {
                    var note = new Note("yo");
                    var note2 = new Note("yay");
                    assertThat(note).isNotEqualTo(note2);
                }
        );
    }

    @Test
    void testHashCode() {
        assertAll(
                () -> assertThat(new Note().hashCode()).isEqualTo(0),
                () -> assertThat(new Note("yo").hashCode()).isEqualTo("yo".hashCode())
        );
    }
}