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
        // SIGURNOST: Uzimamo identitet direktno iz JWT tokena
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // SIGURNOST: Pregazimo bilo šta što je klijent poslao kao sender ID
        message.setSender(currentUser);

        return messageService.sendMessage(message);
    }

    @GetMapping("/history/{u1}/{u2}")
    public List<Message> getHistory(@PathVariable Long u1, @PathVariable Long u2, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // SIGURNOST: Dozvoli pristup SAMO ako je ulogovani korisnik u1 ili u2
        if (!currentUser.getId().equals(u1) && !currentUser.getId().equals(u2)) {
            return new ArrayList<>(); // Vrati prazno, haker ne vidi ništa
        }

        return messageService.getChatHistory(u1, u2);
    }

    @GetMapping("/contacts/{userId}")
    public List<User> getContacts(@PathVariable Long userId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // SIGURNOST: Ne dozvoli da korisnik vidi tuđu listu kontakata
        if (!currentUser.getId().equals(userId)) {
            return new ArrayList<>();
        }

        return messageService.getContactedUsers(userId);
    }
}