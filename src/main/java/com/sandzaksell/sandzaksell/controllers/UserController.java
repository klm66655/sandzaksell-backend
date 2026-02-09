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
        return new LoginResponse(
                token,
                foundUser.getId(),
                foundUser.getUsername(),
                foundUser.getRole()
        );
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        userService.updatePassword(id, newPassword); // Implementiraj enkripciju šifre ovde!
        return ResponseEntity.ok("Lozinka uspešno promenjena");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            // 1. Generiši 6 cifara kod
            String code = String.format("%06d", new Random().nextInt(999999));

            // 2. Sačuvaj u bazu (Ovo radi, videli smo na slici!)
            userService.saveResetCode(email, code);

            // 3. Pošalji na mail unutar try-catch bloka
            // Tako da ako mail server pukne, korisnik ipak dobije 200 OK
            try {
                emailService.sendResetEmail(email, code);
            } catch (Exception mailError) {
                // Loguj grešku u konzolu, ali nemoj prekidati proces
                System.err.println("GRESKA PRI SLANJU MAILA: " + mailError.getMessage());
            }

            // Vraćamo uspeh jer je kod u bazi - frontend će sad preći na Step 2
            return ResponseEntity.ok(Map.of("message", "Kod je generisan. Proverite email ili bazu."));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        try {
            userService.resetPasswordWithCode(email, code, newPassword);
            return ResponseEntity.ok(Map.of("message", "Lozinka uspešno promenjena."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/update-image")
    public ResponseEntity<?> updateProfileImage(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String imageUrl = request.get("profileImageUrl");
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setProfileImageUrl(imageUrl);
        userService.saveUser(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/add-tokens")
    public User addTokens(@PathVariable Long id, @RequestParam Integer amount) {
        return userService.updateTokens(id, amount);
    }
}