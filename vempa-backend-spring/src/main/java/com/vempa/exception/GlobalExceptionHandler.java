package com.vempa.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejarResponseStatus(ResponseStatusException ex) {
        // Antes: no se logueaba nada aquí, por eso los errores no aparecían
        // en los logs de Railway. Ahora sí queda registro de cada error.
        if (ex.getStatusCode().is5xxServerError()) {
            log.error("Error {}: {}", ex.getStatusCode(), ex.getReason(), ex.getCause());
        } else {
            log.warn("Error {}: {}", ex.getStatusCode(), ex.getReason());
        }
        return ResponseEntity.status(ex.getStatusCode())
            .body(Map.of("error", ex.getReason() != null ? ex.getReason() : "Error"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> manejarArchivoDemasiadoGrande(MaxUploadSizeExceededException ex) {
        log.warn("Archivo subido excede el tamaño máximo permitido.");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(Map.of("error", "El archivo es demasiado grande. Máximo permitido: 5MB."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            errores.put(err.getField(), err.getDefaultMessage())
        );
        log.warn("Validación fallida: {}", errores);
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Datos inválidos: " + errores
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarGenerico(Exception ex) {
        // Este es el que se estaba comiendo el motivo real de cualquier 500
        // no controlado explícitamente. Ahora imprime el stack trace completo.
        log.error("Error interno no controlado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Error interno del servidor."));
    }
}