package lazyteam.cooking_hansu.domain.chat.repository;

import aj.org.objectweb.asm.commons.Remapper;
import lazyteam.cooking_hansu.domain.chat.entity.ChatMessage;
import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

//    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id IN :roomIds AND m.createdAt IN (" +
//            "SELECT MAX(m2.createdAt) FROM ChatMessage m2 WHERE m2.chatRoom.id IN :roomIds GROUP BY m2.chatRoom.id)")
//    List<ChatMessage> findLastMessagesByRoomIds(@Param("roomIds") List<UUID> roomIds);

    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomAndCreatedAtAfterOrderByCreatedAtAsc(ChatRoom chatRoom, LocalDateTime createdAtAfter);
}
