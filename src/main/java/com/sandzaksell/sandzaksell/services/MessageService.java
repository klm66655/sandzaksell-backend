package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.MessageRepository;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Message sendMessage(Message message) {
        User sender = userRepository.findById(message.getSender().getId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(message.getReceiver().getId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        message.setSender(sender);
        message.setReceiver(receiver);
        return messageRepository.save(message);
    }

    public List<Message> getChatHistory(Long u1, Long u2) {
        return messageRepository.findChatHistory(u1, u2);
    }

    public List<User> getContactedUsers(Long userId) {
        return messageRepository.findContactedUsers(userId);
    }
}