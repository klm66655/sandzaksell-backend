package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.dto.LoginResponse;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.sandzaksell.sandzaksell.services.EmailService;
import java.util.Random;
import java.security.Principal; // OBAVEZNO DODAJ

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://sandzak-sell-marketplace.vercel.app", allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    // --- LOGIN I REGISTER OSTAJU ISTI ---
    @PostMapping("/login")
    public LoginResponse login(@RequestBody User user) {
        String token = userService.verify(user);
        User foundUser = userService.getUserByUsername(user.getUsername());
        return new LoginResponse(token, foundUser.getId(), foundUser.getUsername(), foundUser.getRole());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // OSNOVNA VALIDACIJA (Miralemova tačka 2)
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Neispravan email format!"));
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lozinka mora imati bar 6 karaktera!"));
        }
        return ResponseEntity.ok(userService.registerUser(user));
    }

    // --- OVDJE JE BILA RUPA - SADA JE ZAKLJUČANO ---
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Principal principal) {
        // principal.getName() ti vraća USERNAME, zato koristi getUserByUsername!
        User currentUser = userService.getUserByUsername(principal.getName());
        String newPassword = request.get("newPassword");

        userService.updatePassword(currentUser.getId(), newPassword);
        return ResponseEntity.ok(Map.of("message", "Lozinka uspešno promenjena"));
    }

    // 2. IZMENA ZA SLIKU
    @PutMapping("/update-image")
    public ResponseEntity<?> updateProfileImage(@RequestBody Map<String, String> request, Principal principal) {
        // Isto i ovde - traži po USERNAME
        User currentUser = userService.getUserByUsername(principal.getName());
        String imageUrl = request.get("profileImageUrl");

        currentUser.setProfileImageUrl(imageUrl);
        userService.saveUser(currentUser);
        return ResponseEntity.ok(currentUser);
    }

    // --- GOOGLE LOGIN I FORGOT PASSWORD OSTAJU SLIČNI ---
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

    // ADMIN SAMO (Ovo bi trebalo zaštititi sa @PreAuthorize("hasRole('ADMIN')"))
    @PutMapping("/{id}/add-tokens")
    public User addTokens(@PathVariable Long id, @RequestParam Integer amount) {
        return userService.updateTokens(id, amount);
    }
}
