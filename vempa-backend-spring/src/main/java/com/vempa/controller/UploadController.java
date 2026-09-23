package com.vempa.controller;

import com.vempa.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    // Debe coincidir con spring.servlet.multipart.max-file-size en application.yml.
    private static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024; // 5MB

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Map<String, String> subir(@RequestParam("imagen") MultipartFile imagen) {
        if (imagen.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se recibió ningún archivo.");
        }
        if (imagen.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new ResponseStatusException(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "La imagen pesa " + (imagen.getSize() / 1024 / 1024) + "MB. El máximo permitido es 5MB."
            );
        }

        log.info("Subiendo imagen '{}' ({} KB)", imagen.getOriginalFilename(), imagen.getSize() / 1024);
        String url = cloudinaryService.subir(imagen);
        return Map.of("url", url);
    }
}