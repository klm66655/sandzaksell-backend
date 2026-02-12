package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.MessageService;
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

    public MessageController(MessageService messageService, UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send")
    public Message send(@RequestBody Message message, Principal principal) {
        if (principal == null) throw new RuntimeException("Niste ulogovani!");

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        // SIGURNOST: Ne dozvoli slanje poruke samom sebi
        if (currentUser.getId().equals(message.getReceiver().getId())) {
            throw new RuntimeException("Ne možete poslati poruku sami sebi!");
        }

        message.setSender(currentUser);
        return messageService.sendMessage(message);
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