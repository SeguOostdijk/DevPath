package com.devpath.api_gateway.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Endpoints públicos sin auth
        if (path.equals("/api/auth/register")) return true;
        if (path.equals("/api/auth/login")) return true;
        if (path.equals("/api/auth/refresh")) return true;
        if (path.equals("/api/ai/chat")) return true;

        // GET público en cursos y categorías
        if (HttpMethod.GET.matches(method)) {
            if (path.startsWith("/api/courses") || path.startsWith("/api/categories")) {
                // Excepto el detalle de una clase: GET /api/courses/{id}/lessons/{lessonId}
                if (!path.matches("/api/courses/\\d+/lessons/\\d+")) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isAdminEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // POST /api/courses
        if (HttpMethod.POST.matches(method) && path.equals("/api/courses")) return true;
        // PUT /api/courses/{id}
        if (HttpMethod.PUT.matches(method) && path.matches("/api/courses/\\d+")) return true;
        // POST /api/courses/{id}/lessons
        if (HttpMethod.POST.matches(method) && path.matches("/api/courses/\\d+/lessons")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Token requerido\"}");
            return;
        }

        String token = header.substring(7);

        if (!jwtUtil.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Token inválido o expirado\"}");
            return;
        }

        Claims claims = jwtUtil.parse(token);
        String role = (String) claims.get("role");

        if (isAdminEndpoint(request) && !"ADMIN".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Acceso denegado\"}");
            return;
        }

        // Propagamos userId y role como headers para los microservicios downstream
        HttpServletRequest mutatedRequest = new HeaderMutatingRequest(request,
                "X-User-Id", String.valueOf(claims.getSubject()),
                "X-User-Role", role);

        filterChain.doFilter(mutatedRequest, response);
    }
}
