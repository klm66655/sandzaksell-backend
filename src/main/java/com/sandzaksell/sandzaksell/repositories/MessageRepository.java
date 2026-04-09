package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 1. ISTORIJA RAZGOVORA - Ostaje isto, koristi objekte
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) " +
            "OR (m.sender.id = :u2 AND m.receiver.id = :u1) ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(@Param("u1") Long user1Id, @Param("u2") Long user2Id);

    // 2. SORTIRANJE KONTAKATA - Ostaje isto
    @Query(value = "SELECT u.* FROM users u " +
            "JOIN (" +
            "    SELECT contact_id, MAX(ts) as last_msg " +
            "    FROM (" +
            "        SELECT receiver_id AS contact_id, timestamp::timestamptz as ts FROM messages WHERE sender_id = :userId " +
            "        UNION ALL " +
            "        SELECT sender_id AS contact_id, timestamp::timestamptz as ts FROM messages WHERE receiver_id = :userId " +
            "    ) all_msgs " +
            "    GROUP BY contact_id" +
            ") final_msgs ON u.id = final_msgs.contact_id " +
            "ORDER BY final_msgs.last_msg DESC", nativeQuery = true)
    List<User> findContactedUsers(@Param("userId") Long userId);

    // 3. SEEN LOGIKA - ISPRAVLJENO: Izbačeno 'Is' jer se polje sada zove 'read'
    List<Message> findByReceiverIdAndSenderIdAndReadFalse(Long receiverId, Long senderId);

    // 4. NAVBAR BADGE - ISPRAVLJENO: Izbačeno 'Is'
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.read = false")
    long countUnreadMessages(@Param("receiverId") Long receiverId);

    // 5. BRZI UPDATE ZA SEEN - Ovo dodajemo da ne bi vrteo petlje u servisu
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.read = true WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.read = false")
    void markAllAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

    // 6. SIGURNOSNA PROVERA
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Message m " +
            "WHERE m.id = :messageId AND (m.sender.id = :userId OR m.receiver.id = :userId)")
    boolean isUserParticipant(@Param("messageId") Long messageId, @Param("userId") Long userId);
}