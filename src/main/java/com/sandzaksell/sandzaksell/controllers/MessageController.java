package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:5173") // Proveri da li je port 5173 (Vite default)
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public Message send(@RequestBody Message message) {
        return messageService.sendMessage(message);
    }

    @GetMapping("/history/{u1}/{u2}")
    public List<Message> getHistory(@PathVariable Long u1, @PathVariable Long u2) {
        return messageService.getChatHistory(u1, u2);
    }

    // NOVO: Endpoint za listu kontakata u Inboxu
    @GetMapping("/contacts/{userId}")
    public List<User> getContacts(@PathVariable Long userId) {
        return messageService.getContactedUsers(userId);
    }
}