package com.acme.iamcafelab.shared.interfaces.rest.support;

import com.acme.iamcafelab.shared.interfaces.rest.resources.MessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;

public abstract class CafeLabScopedExceptionHandlerSupport {

    private static final Logger log = LoggerFactory.getLogger(CafeLabScopedExceptionHandlerSupport.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResource> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON: {}", ex.getMessage());

        String detail = ex.getRootCause() != null
                ? ex.getRootCause().getMessage()
                : ex.getMessage();

        String msg = detail != null && !detail.isBlank()
                ? detail
                : "Cuerpo de solicitud inválido";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResource("No se pudo leer el JSON: " + msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResource> handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("Illegal argument: {}", ex.getMessage());

        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "Solicitud inválida";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResource(msg));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MessageResource> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());

        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "Estado inválido";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResource(msg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResource> handleUnhandledRuntime(RuntimeException ex) {
        String msg = ex.getMessage();

        if (msg != null && msg.toLowerCase().contains("already exists")) {
            log.warn("Duplicate resource detected: {}", msg);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResource(msg));
        }

        log.error("Unhandled runtime in scoped controller advice", ex);

        if (msg == null || msg.isBlank()) {
            msg = "Error interno del servidor";
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResource(msg));
    }
}