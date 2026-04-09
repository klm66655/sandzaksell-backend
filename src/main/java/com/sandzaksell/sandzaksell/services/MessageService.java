package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.MessageRepository;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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
        // Osiguravamo da je primalac validan
        User receiver = userRepository.findById(message.getReceiver().getId())
                .orElseThrow(() -> new RuntimeException("Primalac nije nađen"));

        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());

        // Koristimo .setRead(false) jer se polje u modelu zove 'read'
        message.setRead(false);
        message.setDelivered(true);

        return messageRepository.save(message);
    }

    public List<Message> getChatHistory(Long requesterId, Long otherUserId) {
        return messageRepository.findChatHistory(requesterId, otherUserId);
    }

    public List<User> getContactedUsers(Long userId) {
        return messageRepository.findContactedUsers(userId);
    }

    // NAVBAR BADGE: Usklađeno sa novim imenom metode u Repository-ju (bez 'Is')
    public long getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndReadFalse(userId);
    }

    // SEEN LOGIKA: Optimizovano da jednim SQL potezom sve markira kao pročitano
    @Transactional
    public void markConversationAsRead(Long receiverId, Long senderId) {
        // Više nema petlje unreadMessages.forEach...
        // Pozivamo direktno UPDATE query iz repository-ja
        messageRepository.markAllAsRead(receiverId, senderId);
    }
}