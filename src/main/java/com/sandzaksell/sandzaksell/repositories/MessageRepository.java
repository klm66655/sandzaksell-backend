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

    // 1. Istorija razgovora (Uvek ASC da bi starije bile gore, novije dole)
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) " +
            "OR (m.sender.id = :u2 AND m.receiver.id = :u1) ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(@Param("u1") Long user1Id, @Param("u2") Long user2Id);

    // 2. MODIFIKOVANA LISTA KONTAKATA - Ovo ti rešava problem "poruka na sredini"
    // Ovaj query traži korisnike i odmah ih sortira prema vremenu njihove poslednje zajedničke poruke
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN (" +
            "SELECT m.sender.id FROM Message m WHERE m.receiver.id = :userId " +
            "UNION " +
            "SELECT m.receiver.id FROM Message m WHERE m.sender.id = :userId) " +
            "ORDER BY (SELECT MAX(m2.timestamp) FROM Message m2 WHERE " +
            "(m2.sender.id = u.id AND m2.receiver.id = :userId) OR " +
            "(m2.sender.id = :userId AND m2.receiver.id = u.id)) DESC")
    List<User> findContactedUsers(@Param("userId") Long userId);

    // 3. Brojanje nepročitanih poruka (Za bedž u Navbaru)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.read = false")
    long countUnreadMessages(@Param("userId") Long userId);

    // 4. Pronalaženje nepročitanih da bi ih označili kao pročitane
    List<Message> findByReceiverIdAndReadFalse(Long receiverId);

    // 5. Sve poruke primaoca sortirane po vremenu
    List<Message> findByReceiverIdOrderByTimestampDesc(Long receiverId);
}