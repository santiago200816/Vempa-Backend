package com.vempa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Genera y valida los JWT de VEMPA (tanto de admins como de clientes).
 *
 * - Algoritmo de firma: HS256 (HMAC-SHA256), simétrico: la misma clave (JWT_SECRET)
 *   firma y verifica el token. Por eso esa clave nunca debe exponerse.
 * - El token lleva: subject = email, un claim extra "role" (ADMIN o CLIENTE),
 *   "iat" (fecha de emisión) y "exp" (fecha de expiración) — Firmados, no
 *   encriptados: cualquiera puede LEER el contenido del token (es solo Base64),
 *   pero nadie puede MODIFICARLO sin invalidar la firma.
 * - El "role" es lo que permite diferenciar, en SecurityConfig, qué rutas puede
 *   usar cada tipo de usuario (/api/admin/** solo ADMIN, /api/clientes/** solo
 *   CLIENTE) usando un único mecanismo de autenticación.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(
        @Value("${vempa.jwt.secret}") String secretoBase,
        @Value("${vempa.jwt.expiracion-minutos}") long expiracionMinutos
    ) {
        // La clave HS256 debe tener al menos 256 bits (32 bytes). Si el secreto
        // configurado es más corto, lo normalizamos con SHA-256 para cumplir el
        // requisito de longitud sin exponer un error críptico en producción.
        this.clave = Keys.hmacShaKeyFor(normalizar(secretoBase));
        this.expiracionMs = expiracionMinutos * 60 * 1000;
    }

    private byte[] normalizar(String secreto) {
        byte[] bytes = secreto.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (bytes.length >= 32) return bytes;
        try {
            return java.security.MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo normalizar JWT_SECRET", e);
        }
    }

    public String generarToken(String email, String role) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(ahora)
            .expiration(expira)
            .signWith(clave, Jwts.SIG.HS256)
            .compact();
    }

    public long expiracionEnSegundos() {
        return expiracionMs / 1000;
    }

    /** Lanza excepción (token inválido, alterado o expirado) si no es válido. */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
