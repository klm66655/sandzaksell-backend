package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.dto.LoginResponse;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

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