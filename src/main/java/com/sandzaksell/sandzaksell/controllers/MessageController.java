package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;
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

    @PostMapping("/send")
    public ResponseEntity<Message> send(@RequestBody Message message, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        // 1. Nađi pošiljaoca (ulogovanog korisnika)
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        // 2. Proveri da li šalje samom sebi
        if (message.getReceiver() == null || currentUser.getId().equals(message.getReceiver().getId())) {
            throw new RuntimeException("Neispravan primalac ili šaljete poruku sami sebi!");
        }

        // 3. Postavi pošiljaoca i snimi poruku u bazu PRVO
        message.setSender(currentUser);
        message.setTimestamp(java.time.LocalDateTime.now()); // Dobra praksa ako već nemaš u servisu
        Message savedMessage = messageService.sendMessage(message);

        // 4. TEK SAD šalješ notifikaciju preko WebSocketa
        // Koristimo ID primaoca kao destinaciju
        try {
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getReceiver().getId().toString(),
                    "/queue/notifications",
                    savedMessage // Frontend će primiti ceo JSON poruke
            );
            System.out.println("Notifikacija poslata korisniku: " + savedMessage.getReceiver().getId());
        } catch (Exception e) {
            // Loguj grešku ali nemoj blokirati HTTP odgovor ako socket pukne
            System.err.println("Greška pri slanju WebSocket notifikacije: " + e.getMessage());
        }

        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/history/{u1}/{u2}")
    public List<Message> getHistory(@PathVariable Long u1, @PathVariable Long u2, Principal principal) {
        if (principal == null) throw new RuntimeException("Pristup odbijen!");

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        // SIGURNOST: Ako ulogovani lik nije ni u1 ni u2, IZBACI GA (403 Forbidden)
        if (!currentUser.getId().equals(u1) && !currentUser.getId().equals(u2)) {
            throw new RuntimeException("Nemate dozvolu da čitate ovaj razgovor!");
        }

        return messageService.getChatHistory(u1, u2);
    }

    @GetMapping("/contacts/{userId}")
    public List<User> getContacts(@PathVariable Long userId, Principal principal) {
        if (principal == null) return new ArrayList<>();

        // POPRAVKA: Ovde je bio findByEmail, promenjeno u findByUsername
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        // SIGURNOST: Ne dozvoli da korisnik vidi tuđu listu kontakata
        if (!currentUser.getId().equals(userId)) {
            return new ArrayList<>();
        }

        return messageService.getContactedUsers(userId);
    }
}