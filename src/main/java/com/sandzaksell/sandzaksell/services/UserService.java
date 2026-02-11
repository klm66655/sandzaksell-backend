package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

    // DODAJ OVO U UserService.java
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Korisnik sa tim emailom nije pronađen"));
    }

    @Transactional
    public void saveResetCode(String email, String code) {
        User user = getUserByEmail(email);
        user.setResetCode(code);
        user.setResetCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
    }

    @Transactional
    public void resetPasswordWithCode(String email, String code, String newPassword) {
        User user = getUserByEmail(email);

        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            throw new RuntimeException("Pogrešan kod za resetovanje!");
        }

        if (user.getResetCodeExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Kod je istekao!");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setResetCode(null); // Brišemo kod nakon uspešne promene
        user.setResetCodeExpiresAt(null);
        userRepository.save(user);
    }

    public User processGoogleUser(String email, String name, String googleId, String profileImage) {
        return userRepository.findByEmail(email).map(user -> {
            // Ako korisnik postoji, a nema googleId, dodajemo ga
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                return userRepository.save(user);
            }
            return user;
        }).orElseGet(() -> {
            // Registracija novog korisnika
            User newUser = new User();
            newUser.setEmail(email); // Polje iz tvog modela

            // Za username stavljamo ime sa Google-a, a ako ga nema uzimamo deo mejla
            String suggestedUsername = (name != null && !name.isEmpty()) ? name : email.split("@")[0];
            newUser.setUsername(suggestedUsername);

            newUser.setGoogleId(googleId); // Polje iz tvog modela
            newUser.setProfileImageUrl(profileImage); // Polje iz tvog modela
            newUser.setRole("ROLE_USER"); // Pratimo tvoj default iz modela
            newUser.setTokenBalance(0); // Polje iz tvog modela
            newUser.setEnabled(true); //

            // Lozinka mora biti tu jer je nullable=false u tvom modelu
            newUser.setPassword(new BCryptPasswordEncoder().encode(java.util.UUID.randomUUID().toString()));

            return userRepository.save(newUser);
        });
    }
    // Dodaj ovo u UserService.java
    public String generateTokenForGoogleUser(User user) {
        // Ovde pozivaš tvoj postojeći servis za tokene.
        // Pošto je Google već potvrdio identitet, samo mu dajemo naš JWT za njegov username.
        List<String> roles = Collections.singletonList(user.getRole());
        return jwtService.generateToken(user.getUsername(), roles);
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