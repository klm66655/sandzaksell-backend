package com.sandzaksell.sandzaksell.config;

import com.sandzaksell.sandzaksell.services.JWTService;
import com.sandzaksell.sandzaksell.services.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
    private ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        String username = null;

        // 1. POKUŠAJ DA NAĐEŠ TOKEN U KUKIJU
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) { // Ime mora biti isto kao u UserController-u
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2. AKO NEMA U KUKIJU, POGLEDAJ HEADER (zbog stare verzije frontenda/testiranja)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 3. IZVLAČENJE USERNAME-A AKO TOKEN POSTOJI
        if (token != null) {
            try {
                username = jwtService.extractUserName(token);
            } catch (Exception e) {
                // Ako je token nevažeći, samo nastavi - Security će ga blokirati kasnije
                System.out.println("Greška pri čitanju tokena: " + e.getMessage());
            }
        }

        // 4. AUTENTIFIKACIJA
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(username);

            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Postavljamo korisnika u SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 5. PUSTI ZAHTEV DALJE
        filterChain.doFilter(request, response);
    }
}