package com.swirl.ecomengine.security.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter used for demonstration and portfolio purposes.
 * <pre>
 * This implementation tracks request counts per client IP using a ConcurrentHashMap.
 * It is intentionally minimal and not intended for production use.
 * <pre>
 * Limitations:
 * - In-memory only (no distributed support)
 * - IP-based (not user/token aware)
 * - No sliding window or advanced throttling strategies
 * <pre>
 * Suitable for showcasing rate limiting concepts in a Spring Boot application.
 */
@Component
public class AuthRateLimiter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!request.getServletPath().startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        Attempt attempt = attempts.computeIfAbsent(ip, k -> new Attempt());

        synchronized (attempt) {
            long now = Instant.now().toEpochMilli();

            // Reset window
            if (now - attempt.windowStart > WINDOW_MS) {
                attempt.windowStart = now;
                attempt.count = 0;
            }

            attempt.count++;

            if (attempt.count > MAX_ATTEMPTS) {
                response.setStatus(429); // Too Many Requests
                response.getWriter().write("Too many authentication attempts. Try again later.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class Attempt {
        long windowStart = Instant.now().toEpochMilli();
        int count = 0;
    }
}