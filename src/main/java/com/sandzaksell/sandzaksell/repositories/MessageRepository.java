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

    // 1. ISTORIJA RAZGOVORA - Strogo filtriranje po ID-evima pošiljaoca i primaoca
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) " +
            "OR (m.sender.id = :u2 AND m.receiver.id = :u1) ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(@Param("u1") Long user1Id, @Param("u2") Long user2Id);

    // 2. MODERNO SORTIRANJE KONTAKATA - Onaj ko je zadnji poslao poruku uvek ide na VRH liste.
    // Koristimo MAX(timestamp) da bi SQL odmah poređao usere po najsvežijoj poruci.
    @Query("SELECT u FROM User u WHERE u.id IN (" +
            "SELECT m.sender.id FROM Message m WHERE m.receiver.id = :userId " +
            "UNION " +
            "SELECT m.receiver.id FROM Message m WHERE m.sender.id = :userId) " +
            "ORDER BY (SELECT MAX(m2.timestamp) FROM Message m2 WHERE " +
            "(m2.sender.id = u.id AND m2.receiver.id = :userId) OR " +
            "(m2.sender.id = :userId AND m2.receiver.id = u.id)) DESC")
    List<User> findContactedUsers(@Param("userId") Long userId);

    // 3. SEEN LOGIKA - Pronalazi sve nepročitane poruke koje je poslao određeni korisnik tebi
    // Ovo pozivamo kada uđeš u chat sa tim korisnikom.
    List<Message> findByReceiverIdAndSenderIdAndIsReadFalse(Long receiverId, Long senderId);

    // 4. NAVBAR BADGE - Broji UKUPNE nepročitane poruke za ulogovanog korisnika
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    // 5. SIGURNOSNA PROVERA - Da proverimo da li korisnik uopšte sme da pristupi poruci (za Controller)
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Message m " +
            "WHERE m.id = :messageId AND (m.sender.id = :userId OR m.receiver.id = :userId)")
    boolean isUserParticipant(@Param("messageId") Long messageId, @Param("userId") Long userId);
}