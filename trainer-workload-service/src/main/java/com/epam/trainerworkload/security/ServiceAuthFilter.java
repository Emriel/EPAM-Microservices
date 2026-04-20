package com.epam.trainerworkload.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceAuthFilter extends OncePerRequestFilter {

    private static final String[] PUBLIC_PATHS = { "/actuator", "/swagger-ui", "/v3/api-docs" };

    private final JwtValidationService jwtValidationService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        for (String publicPath : PUBLIC_PATHS) {
            if (requestUri.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String token = header.substring(7);
        if (!jwtValidationService.isValid(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String subject = jwtValidationService.extractSubject(token);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(subject, null,
                Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
