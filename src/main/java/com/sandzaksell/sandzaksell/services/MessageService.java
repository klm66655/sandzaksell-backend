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

        // MODERNO: Početni statusi poruke
        message.setRead(false);       // Seen: false
        message.setDelivered(true);   // Delivered: true (jer je stigla do servera)

        return messageRepository.save(message);
    }

    // SIGURNOST: Samo učesnici razgovora mogu dobiti istoriju
    public List<Message> getChatHistory(Long requesterId, Long otherUserId) {
        return messageRepository.findChatHistory(requesterId, otherUserId);
    }

    // MODERNO: Vraća kontakte sortirane po najnovijoj poruci (naš novi Query)
    public List<User> getContactedUsers(Long userId) {
        return messageRepository.findContactedUsers(userId);
    }

    // NAVBAR BADGE: Broji ukupno nepročitane za celog usera
    public long getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    // SEEN LOGIKA: Markira poruke kao pročitane samo za taj specifičan razgovor
    @Transactional
    public void markConversationAsRead(Long receiverId, Long senderId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndSenderIdAndIsReadFalse(receiverId, senderId);
        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(msg -> msg.setRead(true));
            messageRepository.saveAll(unreadMessages);
        }
    }
}