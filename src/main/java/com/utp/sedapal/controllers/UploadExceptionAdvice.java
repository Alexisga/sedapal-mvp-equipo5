package com.utp.sedapal.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.ResponseEntity;

@ControllerAdvice
public class UploadExceptionAdvice {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> archivoExcedido() {
        return ResponseEntity.status(413).body("La evidencia excede el tamaño permitido de 5 MiB.");
    }
}
