package com.example.Eminent.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad que intercepta cada petición HTTP para extraer y validar
 * el token JWT del header Authorization. Si el token es válido, establece la
 * autenticación en el contexto de seguridad de Spring para que los endpoints
 * protegidos puedan acceder a los detalles del usuario actual.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Utilidad JWT para la validación y extracción de claims del token. */
    @Autowired private JwtUtil jwtUtil;
    /** Servicio para cargar los detalles del usuario por su correo. */
    @Autowired private CustomUserDetailsService userDetailsService;

    /**
     * Intercepta cada petición HTTP y valida el token JWT si está presente
     * en el header Authorization like "Bearer <token>". Establece la autenticación
     * en el SecurityContext si la validación es exitosa.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.esValido(token)) {
                String correo = jwtUtil.extraerCorreo(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(correo);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}