package lazyteam.cooking_hansu.domain.chat.repository;

import org.springframework.data.repository.query.Param;
import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    @Query("""
    SELECT cp.chatRoom
    FROM ChatParticipant cp
    WHERE cp.user.id IN (:userId, :otherUserId)
    GROUP BY cp.chatRoom
    HAVING COUNT(DISTINCT cp.user.id) = 2
    """)
    Optional<ChatRoom> findExistingChatRoom(@Param("userId") UUID userId, @Param("otherUserId") UUID otherUserId);

    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    @Query("""
        SELECT cp
        FROM ChatParticipant cp
        LEFT JOIN ChatMessage cm ON cm.chatRoom = cp.chatRoom
        WHERE cp.user = :user
          AND cp.isActive = 'Y'
        GROUP BY cp.id, cp.chatRoom.id
        ORDER BY
          CASE WHEN MAX(cm.createdAt) IS NULL THEN 1 ELSE 0 END,
          MAX(cm.createdAt) DESC
    """)
    List<ChatParticipant> findMyActiveParticipantsOrderByLastMessage(@Param("user") User user);

    @Query("""
        SELECT cp
        FROM ChatParticipant cp
        LEFT JOIN ChatMessage cm ON cm.chatRoom = cp.chatRoom
        WHERE cp.user = :user
          AND cp.isActive = 'Y'
        GROUP BY cp.id, cp.chatRoom.id
        ORDER BY
          CASE WHEN MAX(cm.createdAt) IS NULL THEN 1 ELSE 0 END,
          MAX(cm.createdAt) DESC
    """)
    Slice<ChatParticipant> findMyActiveParticipantsOrderByLastMessageSlice(@Param("user") User user, Pageable pageable);

    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);

    long countByChatRoomAndIsActive(ChatRoom chatRoom, String isActive);

    List<ChatParticipant> findAllByChatRoomId(Long chatRoomId);
}
