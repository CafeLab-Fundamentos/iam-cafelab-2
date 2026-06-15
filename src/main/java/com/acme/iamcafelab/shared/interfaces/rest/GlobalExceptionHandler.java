package com.acme.iamcafelab.shared.interfaces.rest;

import com.acme.iamcafelab.shared.interfaces.rest.resources.MessageResource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> items = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("field", fieldError.getField());

            String msg = fieldError.getDefaultMessage();
            row.put("message", msg != null && !msg.isBlank() ? msg : "Valor inválido");

            items.add(row);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Error de validación");
        body.put("errors", items);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResource> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage();

        if (msg == null || msg.isBlank()) {
            msg = "Solicitud inválida";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResource(msg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResource> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage();

        if (msg != null && msg.toLowerCase().contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResource(msg));
        }

        if (msg == null || msg.isBlank()) {
            msg = "Error interno del servidor";
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResource(msg));
    }
}