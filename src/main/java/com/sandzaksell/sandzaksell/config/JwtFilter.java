package com.sandzaksell.sandzaksell.config;

import com.sandzaksell.sandzaksell.services.JWTService;
import com.sandzaksell.sandzaksell.services.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. LOG I OPTIONS CHECK (CORS Preflight)
        System.out.println("DEBUG: Dolazni zahtev: " + request.getMethod() + " " + request.getRequestURI());

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // Pustamo OPTIONS da prođe bez ikakve provere tokena.
            // Headere će dodati CorsFilter koji smo podesili u SecurityConfig.
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            // 2. IZVLAČENJE TOKENA
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtService.extractUserName(token);
            }

            // 3. AUTENTIFIKACIJA AKO JE TOKEN TU
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Provera da li je korisnik banovan
                if (!userDetails.isEnabled()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"message\": \"Account is disabled.\"}");
                    return;
                }

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Postavljamo korisnika u SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("DEBUG: Autentifikacija uspešna za: " + username);
                }
            }
        } catch (Exception e) {
            // Ako token istekne ili je neispravan, samo ispišemo log, ne rušimo aplikaciju.
            // Spring Security će svakako vratiti 401 jer Authentication neće biti u kontekstu.
            System.out.println("DEBUG: JWT Error -> " + e.getMessage());
        }

        // 4. NASTAVAK LANCA FILTERA
        filterChain.doFilter(request, response);
    }
}