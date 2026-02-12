package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.dto.LoginResponse;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.UserService;
import com.sandzaksell.sandzaksell.services.EmailService;
import jakarta.servlet.http.Cookie; // BITNO
import jakarta.servlet.http.HttpServletResponse; // BITNO
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Random;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://sandzak-sell-marketplace.vercel.app", allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody User user, HttpServletResponse response) {
        String token = userService.verify(user);
        User foundUser = userService.getUserByUsername(user.getUsername());

        // KREIRANJE KUKIJA
        Cookie jwtCookie = new Cookie("token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // setuj na false ako testiraš lokalno bez HTTPS-a
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600); // 1 sat
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(new LoginResponse(null, foundUser.getId(), foundUser.getUsername(), foundUser.getRole()));
    }

    @PostMapping("/google-login")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody Map<String, String> payload, HttpServletResponse response) {
        String email = payload.get("email");
        String name = payload.get("name");
        String googleId = payload.get("googleId");
        String profileImage = payload.get("profileImage");

        User user = userService.processGoogleUser(email, name, googleId, profileImage);
        String token = userService.generateTokenForGoogleUser(user);

        // KREIRANJE KUKIJA I ZA GOOGLE
        Cookie jwtCookie = new Cookie("token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(new LoginResponse(null, user.getId(), user.getUsername(), user.getRole()));
    }

    // DODAJEMO LOGOUT DA OBRSTI KUKI
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Odmah uništava kuki
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(Map.of("message", "Odjavljeni ste"));
    }

    // --- OSTALE METODE (Register, Change Password, itd.) OSTAJU ISTE ---
    // One već koriste Principal ili ID, tako da ih ne diramo.

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Neispravan email format!"));
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lozinka mora imati bar 6 karaktera!"));
        }
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        String newPassword = request.get("newPassword");
        userService.updatePassword(currentUser.getId(), newPassword);
        return ResponseEntity.ok(Map.of("message", "Lozinka uspešno promenjena"));
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}