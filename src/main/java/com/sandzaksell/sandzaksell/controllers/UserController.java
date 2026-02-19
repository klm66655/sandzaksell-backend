package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.dto.LoginResponse;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.UserService;
import com.sandzaksell.sandzaksell.services.EmailService;
import jakarta.validation.Valid; // OBAVEZNO DODAJ
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://sandzak-sell-marketplace.vercel.app", allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody User user) {
        String token = userService.verify(user);
        User foundUser = userService.getUserByUsername(user.getUsername());
        return new LoginResponse(token, foundUser.getId(), foundUser.getUsername(), foundUser.getRole());
    }

    // --- SADA KORISTI @Valid IZ MODELA ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        // Više ti ne trebaju ručni "if" uslovi ovde!
        // @Valid će automatski proveriti email format i dužinu lozinke
        // na osnovu onoga što smo upisali u User.java klasu.
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        String newPassword = request.get("newPassword");

        // Ovde možeš dodati proveru za dužinu nove lozinke ako želiš
        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nova lozinka mora imati bar 8 karaktera!"));
        }

        userService.updatePassword(currentUser.getId(), newPassword);
        return ResponseEntity.ok(Map.of("message", "Lozinka uspešno promenjena"));
    }

    @PutMapping("/update-image")
    public ResponseEntity<?> updateProfileImage(@RequestBody Map<String, String> request, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        String imageUrl = request.get("profileImageUrl");

        currentUser.setProfileImageUrl(imageUrl);
        userService.saveUser(currentUser);
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/google-login")
    public LoginResponse googleLogin(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.get("name");
        String googleId = payload.get("googleId");
        String profileImage = payload.get("profileImage");
        User user = userService.processGoogleUser(email, name, googleId, profileImage);
        String token = userService.generateTokenForGoogleUser(user);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            String code = String.format("%06d", new Random().nextInt(999999));
            userService.saveResetCode(email, code);
            new Thread(() -> {
                try { emailService.sendResetEmail(email, code); }
                catch (Exception e) { System.err.println("Greška: " + e.getMessage()); }
            }).start();
            return ResponseEntity.ok(Map.of("message", "Kod je poslat."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            userService.resetPasswordWithCode(request.get("email"), request.get("code"), request.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Lozinka promenjena."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/add-tokens")
    @PreAuthorize("hasRole('ADMIN')")
    public User addTokens(@PathVariable Long id, @RequestParam Integer amount) {
        return userService.updateTokens(id, amount);
    }
}