package com.sandzaksell.sandzaksell.config;

import com.sandzaksell.sandzaksell.services.JWTService;
import com.sandzaksell.sandzaksell.services.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
    private MyUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Loguj svaki dolazni zahtev da vidimo rutu
        System.out.println("DEBUG: Dolazni zahtev na: " + request.getMethod() + " " + request.getRequestURI());

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", "https://sandzak-sell-marketplace.vercel.app");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Origin, Accept");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 1. Provera Header-a
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtService.extractUserName(token);
                System.out.println("DEBUG: Username izvučen iz tokena: " + username);
            } catch (Exception e) {
                System.out.println("DEBUG: Greška pri čitanju tokena: " + e.getMessage());
            }
        } else {
            // Ako nema tokena, a ruta traži login, ovde je problem
            System.out.println("DEBUG: Nema Authorization Bearer header-a!");
        }

        // 2. Autentifikacija
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // --- OVDE IDU TVOJE LINIJE ---
            System.out.println("DEBUG: Učitan korisnik iz baze: " + userDetails.getUsername());
            System.out.println("DEBUG: Role korisnika u Security: " + userDetails.getAuthorities());

            if (!userDetails.isEnabled()) {
                System.out.println("DEBUG: Korisnik je BANovan!");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"message\": \"Vaš nalog je banovan.\"}");
                return;
            }

            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("DEBUG: Autentifikacija USPEŠNO postavljena u SecurityContext!");
            } else {
                System.out.println("DEBUG: Token NIJE VALIDAN (možda je istekao ili je SecretKey promenjen)");
            }
        }

        // 3. Pusti zahtev dalje ka kontroleru
        filterChain.doFilter(request, response);
    }
}
