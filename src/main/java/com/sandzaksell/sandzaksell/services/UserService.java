package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEnabled(true);
        if (user.getTokenBalance() == null) user.setTokenBalance(0);
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        } else if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole());
        }
        return userRepository.save(user);
    }

    public String verify(User user) {
        User dbUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik ne postoji!"));

        if (dbUser.getEnabled() == null || !dbUser.getEnabled()) {
            throw new RuntimeException("Nalog je banovan ili nije aktiviran!");
        }

        if (!encoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new BadCredentialsException("Pogrešna lozinka!");
        }

        return jwtService.generateToken(dbUser.getUsername(), List.of(dbUser.getRole()));
    }

    @Transactional
    public void banUser(Long userId) {
        User user = getUserById(userId);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public User updateTokens(Long userId, Integer amount) {
        User user = getUserById(userId);
        int newBalance = (user.getTokenBalance() != null ? user.getTokenBalance() : 0) + amount;
        user.setTokenBalance(newBalance);
        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Enkriptujemo novu lozinku pre čuvanja
        user.setPassword(encoder.encode(newPassword));

        userRepository.save(user);
    }

    // OVO JE METODA KOJA TI JE PRAVILA ERROR
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen: " + username));
    }
}