package com.champsoft.vrms.agents.api;

import com.champsoft.vrms.agents.application.exception.*;
import com.champsoft.vrms.agents.domain.exception.*;
import com.champsoft.vrms.agents.web.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(assignableTypes = AgentController.class)
public class AgentExceptionHandler {

    @ExceptionHandler(AgentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(AgentNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex, req);
    }

    @ExceptionHandler(DuplicateAgentException.class)
    public ResponseEntity<ApiErrorResponse> conflict(DuplicateAgentException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex, req);
    }

    @ExceptionHandler({
            InvalidRoleException.class,
            InvalidAgentNameException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> badRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex, req);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> badJson(org.springframework.http.converter.HttpMessageNotReadableException ex,
                                                   HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, new IllegalArgumentException(toReadableMessage(ex)), req);
    }

    private String toReadableMessage(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {
                if (ife.getTargetType() == com.champsoft.vrms.agents.domain.model.Role.class) {
                    var bad = ife.getValue();
                    return "invalid role: " + (bad == null ? "<null>" : bad) + ". Allowed values: CLERK, SUPERVISOR";
                }
            }
            if (current instanceof com.fasterxml.jackson.databind.JsonMappingException jme) {
                boolean roleField = jme.getPath().stream()
                        .anyMatch(ref -> "role".equals(ref.getFieldName()));
                if (roleField) {
                    return "invalid role. Allowed values: CLERK, SUPERVISOR";
                }
            }
            current = current.getCause();
        }
        String msg = null;
        if (ex.getMostSpecificCause() != null) {
            msg = ex.getMostSpecificCause().getMessage();
        }
        if (msg == null) {
            msg = ex.getMessage();
        }
        if (msg != null && msg.contains("Role") && msg.contains("CLERK") && msg.contains("SUPERVISOR")) {
            return "invalid role. Allowed values: CLERK, SUPERVISOR";
        }
        return "Invalid JSON payload";
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, Exception ex, HttpServletRequest req) {
        var body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
