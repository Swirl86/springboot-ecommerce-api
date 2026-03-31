package com.swirl.ecomengine.security.jwt;

import com.swirl.ecomengine.security.userdetails.CustomUserDetails;
import com.swirl.ecomengine.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Profile({"dev", "prod", "test-integration"})
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

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

        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwtService.validateToken(token);

            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            if (email == null || role == null || isAlreadyAuthenticated()) {
                filterChain.doFilter(request, response);
                return;
            }

            userRepository.findByEmail(email).ifPresent(user -> {
                var auth = buildAuthentication(user, role, request);
                SecurityContextHolder.getContext().setAuthentication(auth);
            });

        } catch (Exception ignored) {
            // Invalid token → SecurityConfig handles 401
        }

        filterChain.doFilter(request, response);
    }

    // ------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------

    private boolean isPublicEndpoint(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }

    private boolean isAlreadyAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(
            com.swirl.ecomengine.user.User user,
            String role,
            HttpServletRequest request
    ) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        var userDetails = new CustomUserDetails(user);

        var auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
        );

        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return auth;
    }
}