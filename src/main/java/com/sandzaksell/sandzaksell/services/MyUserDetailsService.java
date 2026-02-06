package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // POPRAVKA: Dodat orElseThrow jer je findByUsername sada Optional
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen: " + username));

        // Osiguravamo da role ima prefiks ROLE_ jer Spring Security to zahteva za hasRole
        String userRole = user.getRole();
        if (userRole != null && !userRole.startsWith("ROLE_")) {
            userRole = "ROLE_" + userRole;
        }

        // Vraćamo Springov User objekat
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled() != null ? user.getEnabled() : true,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(userRole))
        );
    }
}