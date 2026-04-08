package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 1. Istorija razgovora
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) " +
            "OR (m.sender.id = :u2 AND m.receiver.id = :u1) ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(@Param("u1") Long user1Id, @Param("u2") Long user2Id);

    // 2. Lista kontakata (Korisnici sa kojima je bilo razmene poruka)
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN (" +
            "SELECT m.sender.id FROM Message m WHERE m.receiver.id = :userId " +
            "UNION " +
            "SELECT m.receiver.id FROM Message m WHERE m.sender.id = :userId)")
    List<User> findContactedUsers(@Param("userId") Long userId);

    // 3. Sve poruke primaoca sortirane po vremenu
    List<Message> findByReceiverIdOrderByTimestampDesc(Long receiverId);

    // 4. Pronalaženje svih nepročitanih poruka za korisnika
    List<Message> findByReceiverIdAndReadFalse(Long receiverId);

    // 5. Brojanje nepročitanih poruka (za bedž na Navbaru)
    long countByReceiverIdAndReadFalse(Long receiverId);

    // 6. Provera poruka između dva korisnika (alternativna metoda)
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(Long s1, Long r1, Long s2, Long r2);
}