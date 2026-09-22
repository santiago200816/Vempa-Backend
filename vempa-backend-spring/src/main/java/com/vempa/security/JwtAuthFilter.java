package com.vempa.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Se ejecuta en cada petición: si viene un header "Authorization: Bearer <token>"
 * válido, marca al usuario como autenticado para esta petición, con el rol
 * (ADMIN o CLIENTE) que venga dentro del token — sin sesión ni cookies, JWT es
 * "stateless": el propio token lleva toda la prueba de identidad y permisos.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7);
            try {
                Claims claims = jwtService.validarYObtenerClaims(token);
                String email = claims.getSubject();
                String rol = claims.get("role", String.class);

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                // Token inválido/expirado: no autenticamos. La cadena de seguridad
                // se encarga de devolver 401/403 si la ruta lo requería.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
