package com.github.jorgebsa.spring.demo.ws;

import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.github.jorgebsa.spring.demo.base.SaveNoteRequest;
import com.github.jorgebsa.spring.demo.base.UpdateNoteRequest;
import com.github.jorgebsa.spring.demo.service.NoteFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static org.springframework.http.ResponseEntity.ok;

@Validated
@RestController
@RequestMapping(value = "notes", produces = MediaType.APPLICATION_JSON_VALUE)
class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);

    private final NoteFacade noteFacade;

    NoteController(NoteFacade noteFacade) {
        this.noteFacade = noteFacade;
    }

    @GetMapping
    public ResponseEntity<Page<NoteDTO>> findAll(Pageable pageable) {
        var page = noteFacade.getPage(pageable);
        return ok(page);
    }

    @PostMapping
    public ResponseEntity<?> saveNote(@Valid @RequestBody SaveNoteRequest request) {
        var username = "some-user";
        log.trace("Receiving save note request from [{}]", username);
        var response = noteFacade.saveNote(request, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<NoteDTO> findNote(@NotBlank @PathVariable String id) {
        var found = noteFacade.findById(id);
        return ResponseEntity.of(found);
    }

    @PutMapping("{id}")
    public ResponseEntity<NoteDTO> updateNote(@Valid @RequestBody UpdateNoteRequest request, @NotBlank @PathVariable String id) {
        var username = "some-user";
        log.trace("Receiving update note request from [{}]", username);
        var updated = noteFacade.updateNote(request, username, id);
        return ResponseEntity.of(updated);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<NoteDTO> removeNote(@NotBlank @PathVariable String id) {
        var removed = noteFacade.removeNote(id);
        return ResponseEntity.of(removed);
    }
}
