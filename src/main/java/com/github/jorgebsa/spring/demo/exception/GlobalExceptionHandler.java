package com.github.jorgebsa.spring.demo.exception;

import com.github.jorgebsa.spring.demo.validation.ErrorMessage;
import com.github.jorgebsa.spring.demo.validation.Violation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@ControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorMessage> handleConstraintValidationException(ConstraintViolationException ex) {
        var status = BAD_REQUEST;
        log.info("Handling ConstraintViolationException [{}]", status);
        var violations = ex.getConstraintViolations()
                .stream()
                .map(v -> {
                    var path = v.getPropertyPath().toString();
                    var idx = path.lastIndexOf('.') + 1;
                    return new Violation(path.substring(idx), v.getMessage());
                })
                .sorted(Violation.COMPARATOR)
                .toList();
        log.debug("Violations: {}", violations);
        return buildResponse(status, violations);
    }

    @ExceptionHandler(IncorrectVersionException.class)
    ResponseEntity<ErrorMessage> handleIncorrectVersionException(IncorrectVersionException ex) {
        log.warn("Handling IncorrectVersionException: User [{}] failed to update note [{}] using version [{}]", ex.getUsername(), ex.getNoteId(), ex.getVersion());
        return buildResponse(CONFLICT, List.of(new Violation("version", "incorrect value: " + ex.getVersion())));
    }

    @ExceptionHandler(value = NotSameUserException.class)
    ResponseEntity<ErrorMessage> handleNotSameUserException(NotSameUserException ex) {
        log.warn("Handling NotSameUserException: [{}] tried to alter note [{}] that belongs to [{}]", ex.getTrespasser(), ex.getNoteId(), ex.getNoteOwner());
        return buildResponse(FORBIDDEN, List.of(new Violation("id", "note [" + ex.getNoteId() + "] does not belong to you")));
    }

    private ResponseEntity<ErrorMessage> buildResponse(HttpStatus status, List<Violation> violations) {
        var body = new ErrorMessage(status.value(), violations);
        return new ResponseEntity<>(body, status);
    }

}