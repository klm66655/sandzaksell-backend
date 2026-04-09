package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "https://sandzak-sell-marketplace.vercel.app")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageService messageService, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // 1. SLANJE PORUKE (Secured)
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody Message message, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Niste autorizovani");

        // Uzimamo pošiljaoca IZ TOKENA, ne iz body-ja (Security!)
        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        if (message.getReceiver() == null || sender.getId().equals(message.getReceiver().getId())) {
            return ResponseEntity.badRequest().body("Neispravan primalac");
        }

        message.setSender(sender);
        Message savedMessage = messageService.sendMessage(message);

        // REAL-TIME: Šaljemo preko WebSocketa primaocu
        try {
            String destination = "/topic/messages/" + savedMessage.getReceiver().getId();
            messagingTemplate.convertAndSend(destination, savedMessage);

            // Šaljemo i notifikaciju za bedž u Navbaru
            messagingTemplate.convertAndSend("/topic/notif-count/" + savedMessage.getReceiver().getId(), "new");
        } catch (Exception e) {
            System.err.println("WS Error: " + e.getMessage());
        }

        return ResponseEntity.ok(savedMessage);
    }

    // 2. ISTORIJA RAZGOVORA (Secured)
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<Message>> getHistory(@PathVariable Long otherUserId, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        // Koristimo ID iz tokena kao u1, a path variable kao u2
        List<Message> history = messageService.getChatHistory(currentUser.getId(), otherUserId);
        return ResponseEntity.ok(history);
    }

    // 3. LISTA KONTAKATA (Secured & Sorted)
    @GetMapping("/contacts")
    public ResponseEntity<List<User>> getContacts(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        // Vraća sortirano: ko ti je zadnji pisao, taj je prvi na listi
        return ResponseEntity.ok(messageService.getContactedUsers(currentUser.getId()));
    }

    // 4. NAVBAR UNREAD COUNT
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Principal principal) {
        if (principal == null) return ResponseEntity.ok(0L);
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        return ResponseEntity.ok(messageService.getUnreadCount(currentUser.getId()));
    }

    // 5. SEEN - MARK AS READ
    @PostMapping("/mark-seen/{senderId}")
    public ResponseEntity<?> markSeen(@PathVariable Long senderId, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        messageService.markConversationAsRead(currentUser.getId(), senderId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));
        messageService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }
}