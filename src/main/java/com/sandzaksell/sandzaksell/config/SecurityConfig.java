package com.sandzaksell.sandzaksell.config;
import org.springframework.beans.factory.annotation.Autowired;
import com.sandzaksell.sandzaksell.config.JwtFilter; // Proveri putanju!
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(UserDetailsService userDetailsService, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // 1. OPTIONS mora biti prvi zbog CORS-a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Eksplicitno dozvoli login i register
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/users/register").permitAll()

                        .requestMatchers("/api/users/forgot-password").permitAll()
                        .requestMatchers("/api/users/reset-password").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/ads/*/view").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/reviews/user/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/add").authenticated()


                        .requestMatchers("/api/tokens/add").authenticated()

                        // 3. Dozvoli GET metode za oglase da bi sajt bio vidljiv i bez logina
                        .requestMatchers(HttpMethod.GET, "/api/ads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()



                        .requestMatchers(HttpMethod.PUT, "/api/ads/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/ads/**").authenticated()

                        .requestMatchers(HttpMethod.PUT, "/api/ads/*/make-premium").authenticated()

                        // 4. Admin rute
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 5. Sve ostalo zahteva login
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Dozvoli i 5173 (Vite) i 8081 (gde ti je React trenutno)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8081", "https://sandzaksell-backend-production.up.railway.app", "https://sandzak-sell-marketplace.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type","Origin",
                "Accept", "Cache-Control", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Ako tvoja verzija traži argument u konstruktoru, daj mu ga odmah:
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));


        // Ako setUserDetailsService i dalje ne radi, a prosledio si ga gore u zagradi,
        // onda ti ova linija ispod više i ne treba!
        // provider.setUserDetailsService(userDetailsService);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}