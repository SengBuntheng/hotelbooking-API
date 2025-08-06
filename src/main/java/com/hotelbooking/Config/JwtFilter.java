package com.hotelbooking.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip JWT filter for public endpoints
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedError(response, "Missing or invalid Authorization header");
            return;
        }

        jwt = authHeader.substring(7);

        if (jwt.isEmpty() || jwt.split("\\.").length < 3) {
            sendUnauthorizedError(response, "Invalid JWT token format");
            return;
        }

        try {
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtService.validateToken(jwt, userDetails)) {
                    setAuthenticationInContext(request, userDetails);
                } else {
                    sendUnauthorizedError(response, "Invalid JWT token");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("JWT processing error", e);
            sendUnauthorizedError(response, "Invalid JWT token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // *** THIS IS THE CORRECTED METHOD ***
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        // Ensure this matches the public endpoints in your SecurityConfig
        return path.startsWith("/v1/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/v1/aba/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/ws-payment/");
    }

    private void setAuthenticationInContext(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}