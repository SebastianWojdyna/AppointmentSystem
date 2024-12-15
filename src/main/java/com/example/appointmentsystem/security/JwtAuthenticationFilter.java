package com.example.appointmentsystem.security;

import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Logowanie nagłówków
        String authHeader = httpRequest.getHeader("Authorization");
        logger.info("Authorization header: {}", authHeader);

        String token = getJwtFromRequest(httpRequest);
        logger.info("Extracted JWT token: {}", token);

        if (StringUtils.hasText(token)) {
            try {
                // Wyciągnij email i rolę z tokena
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                logger.info("Token details - Email: {}, Role: {}", email, role);

                // Znajdź użytkownika w bazie danych
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
                logger.info("User found in database: {}", user.getEmail());

                // Ustaw rolę z prefiksem ROLE_
                String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                logger.info("Setting role with prefix: {}", roleWithPrefix);

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix));

                // Tworzenie autoryzacji
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));

                // Ustawienie kontekstu bezpieczeństwa
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authentication set in SecurityContextHolder: User={}, Role={}", email, roleWithPrefix);

            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                logger.error("Error during JWT authentication: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("No JWT token found in the request.");
        }

        chain.doFilter(request, response);
    }

    /**
     * Pobiera token JWT z nagłówka Authorization.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.info("Received Authorization header: {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String extractedToken = bearerToken.substring(7);
            logger.info("Extracted Bearer token: {}", extractedToken);
            return extractedToken;
        }
        logger.warn("No valid Bearer token found in Authorization header.");
        return null;
    }
}
