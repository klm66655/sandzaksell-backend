package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Message;
import com.sandzaksell.sandzaksell.models.User; // PAZI: mora tvoj model, ne Security User!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Izvlači istoriju poruka između dva korisnika
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) " +
            "OR (m.sender.id = :u2 AND m.receiver.id = :u1) ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(@Param("u1") Long user1Id, @Param("u2") Long user2Id);

    // Izvlači listu korisnika sa kojima je osoba pričala (za listu kontakata u Inboxu)
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN (" +
            "SELECT m.sender.id FROM Message m WHERE m.receiver.id = :userId " +
            "UNION " +
            "SELECT m.receiver.id FROM Message m WHERE m.sender.id = :userId)")
    List<User> findContactedUsers(@Param("userId") Long userId);

    // Nalazi sve poruke za određenog primaoca
    List<Message> findByReceiverIdOrderByTimestampDesc(Long receiverId);
}