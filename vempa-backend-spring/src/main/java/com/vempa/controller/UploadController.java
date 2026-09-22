package com.vempa.controller;

import com.vempa.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Map<String, String> subir(@RequestParam("imagen") MultipartFile imagen) {
        if (imagen.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se recibió ningún archivo.");
        }
        String url = cloudinaryService.subir(imagen);
        return Map.of("url", url);
    }
}
