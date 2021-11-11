package com.github.jorgebsa.spring.demo.ws;

import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.github.jorgebsa.spring.demo.base.SaveNoteRequest;
import com.github.jorgebsa.spring.demo.base.SaveNoteResponse;
import com.github.jorgebsa.spring.demo.base.UpdateNoteRequest;
import com.github.jorgebsa.spring.demo.service.NoteFacade;
import com.github.jorgebsa.spring.demo.validation.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.annotations.ParameterObject;
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
@Tag(name = "notes", description = "The Notes API")
@RequestMapping(value = "notes", produces = MediaType.APPLICATION_JSON_VALUE)
class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);

    private final NoteFacade noteFacade;

    NoteController(NoteFacade noteFacade) {
        this.noteFacade = noteFacade;
    }

    @GetMapping
    @Operation(summary = "Gets a page of Notes", description = "Gets a page of Notes", tags = "notes")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation"
            )
    })
    public ResponseEntity<Page<NoteDTO>> getPage(@ParameterObject Pageable pageable) {
        var page = noteFacade.getPage(pageable);
        return ok(page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Saves a new Note", description = "Saves a new Note", tags = "notes")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaveNoteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid content",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    public ResponseEntity<SaveNoteResponse> saveNote(@Valid @RequestBody SaveNoteRequest request) {
        var username = "some-user";
        log.trace("Receiving save note request from [{}]", username);
        var response = noteFacade.saveNote(request, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    @Operation(summary = "Find Note by ID", description = "Returns a single Note", tags = "notes")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = NoteDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content
            )
    })
    public ResponseEntity<NoteDTO> findNote(@NotBlank @PathVariable String id) {
        var found = noteFacade.findById(id);
        return ResponseEntity.of(found);
    }

    @PutMapping("{id}")
    @Operation(summary = "Updates an existing Note", description = "Updates an existing Note", tags = "notes")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoteDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid content",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Requester is not the note's owner",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Note's version is outdated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    public ResponseEntity<NoteDTO> updateNote(@Valid @RequestBody UpdateNoteRequest request, @NotBlank @PathVariable String id) {
        var username = "some-user";
        log.trace("Receiving update note request from [{}]", username);
        var updated = noteFacade.updateNote(request, username, id);
        return ResponseEntity.of(updated);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletes an existing Note", description = "Deletes an existing Note", tags = "notes")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoteDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content
            )
    })
    public ResponseEntity<NoteDTO> removeNote(@NotBlank @PathVariable String id) {
        var removed = noteFacade.removeNote(id);
        return ResponseEntity.of(removed);
    }
}
