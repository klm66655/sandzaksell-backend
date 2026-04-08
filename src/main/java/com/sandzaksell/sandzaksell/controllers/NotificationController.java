package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Notification;
import com.sandzaksell.sandzaksell.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // Dohvati sve notifikacije za ulogovanog korisnika
    @GetMapping("/user/{userId}")
    public List<Notification> getNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Broj nepročitanih za "badge" na zvoncu
    @GetMapping("/unread-count/{userId}")
    public long getUnreadCount(@PathVariable Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // Obeleži sve kao pročitane
    @PostMapping("/mark-as-read/{userId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok().build();
    }
}