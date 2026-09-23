package com.vempa.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;

    public CloudinaryService(
        @Value("${vempa.cloudinary.cloud-name}") String cloudName,
        @Value("${vempa.cloudinary.api-key}") String apiKey,
        @Value("${vempa.cloudinary.api-secret}") String apiSecret
    ) {
        validarCredencial("CLOUDINARY_CLOUD_NAME", cloudName);
        validarCredencial("CLOUDINARY_API_KEY", apiKey);
        validarCredencial("CLOUDINARY_API_SECRET", apiSecret);

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));

        log.info("Cloudinary configurado correctamente (cloud_name={})", cloudName);
    }

    /**
     * Falla al ARRANCAR la aplicación (no en el primer intento de subida) si
     * alguna credencial de Cloudinary falta o quedó con el valor de ejemplo
     * del .env.example. Así el error aparece claro en los logs de arranque en
     * Railway, en vez de un 500 genérico cuando alguien intenta subir una foto.
     */
    private void validarCredencial(String nombreVariable, String valor) {
        if (valor == null || valor.isBlank() || valor.startsWith("tu_")) {
            throw new IllegalStateException(
                "Falta configurar la variable de entorno " + nombreVariable +
                " (o quedó con el valor de ejemplo del .env.example). " +
                "Configúrala en Railway -> tu servicio -> Variables, con el valor real de tu cuenta " +
                "de Cloudinary (cloudinary.com/console)."
            );
        }
    }

    @SuppressWarnings("unchecked")
    public String subir(MultipartFile archivo) {
        try {
            Map<String, Object> resultado = cloudinary.uploader().upload(
                archivo.getBytes(),
                ObjectUtils.asMap("folder", "vempa/productos")
            );
            return (String) resultado.get("secure_url");
        } catch (Exception e) {
            // Antes este error se perdía silenciosamente (nunca se logueaba).
            // Ahora queda registrado en los logs de Railway con el motivo real
            // (credenciales inválidas, archivo corrupto, timeout de red, etc.).
            log.error("Error al subir imagen a Cloudinary: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo subir la imagen a Cloudinary: " + e.getMessage(),
                e
            );
        }
    }
}