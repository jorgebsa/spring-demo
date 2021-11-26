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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import java.security.Principal;

import static com.github.jorgebsa.spring.demo.ws.OpenAPIConfig.SCHEME_NAME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@Validated
@RestController
@SecurityRequirement(name = SCHEME_NAME)
@Tag(description = "The Notes API", name = "notes")
@RequestMapping(value = "notes", produces = APPLICATION_JSON_VALUE)
class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);

    private final NoteFacade noteFacade;

    NoteController(NoteFacade noteFacade) {
        this.noteFacade = noteFacade;
    }

    @Operation(
            summary = "Gets a page of Notes",
            description = "Gets a page of Notes",
            tags = "notes"
    )
    @GetMapping
    public ResponseEntity<Page<NoteDTO>> getPage(@ParameterObject Pageable pageable) {
        var page = noteFacade.getPage(pageable);
        return ok(page);
    }

    @Operation(
            summary = "Creates a new Note",
            description = "Allows the requester to create a new Note in his collection",
            tags = "notes"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Note was created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid content",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<SaveNoteResponse> saveNote(@Valid @RequestBody SaveNoteRequest request, Principal principal) {
        var username = principal.getName();
        log.trace("Receiving save note request from [{}]", username);
        var response = noteFacade.saveNote(request, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Find Note by ID",
            description = "Returns a single Note if it's found",
            tags = "notes"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Note was retrieved"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note was not found",
                    content = @Content
            )
    })
    @GetMapping("{id}")
    public ResponseEntity<NoteDTO> findNote(@NotBlank @PathVariable String id) {
        var found = noteFacade.findById(id);
        return ResponseEntity.of(found);
    }

    @Operation(
            summary = "Updates an existing Note",
            description = "Updates an existing Note if the requester is the Note's owner",
            tags = "notes"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Note was updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid content",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Requester is not the note's owner",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Note's version is outdated",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @PutMapping(value = "{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<NoteDTO> updateNote(@Valid @RequestBody UpdateNoteRequest request, @NotBlank @PathVariable String id, Principal principal) {
        var username = principal.getName();
        log.trace("Receiving update note request from [{}]", username);
        var updated = noteFacade.updateNote(request, username, id);
        return ResponseEntity.of(updated);
    }

    @Operation(
            summary = "Deletes an existing Note",
            description = "Deletes an existing Note if it's found",
            tags = "notes"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Note was deleted"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content
            )
    })
    @DeleteMapping("{id}")
    public ResponseEntity<NoteDTO> removeNote(@NotBlank @PathVariable String id) {
        var removed = noteFacade.removeNote(id);
        return ResponseEntity.of(removed);
    }
}
