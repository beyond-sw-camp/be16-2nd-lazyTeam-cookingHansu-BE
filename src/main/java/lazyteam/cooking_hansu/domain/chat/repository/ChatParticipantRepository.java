package lazyteam.cooking_hansu.domain.chat.repository;

import io.lettuce.core.dynamic.annotation.Param;
import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    @Query("""
    SELECT cp.chatRoom
    FROM ChatParticipant cp
    WHERE cp.user.id IN (:userId1, :userId2)
    GROUP BY cp.chatRoom
    HAVING COUNT(DISTINCT cp.user.id) = 2
""")
    Optional<ChatRoom> findExistingChatRoom(@Param("userId") UUID userId, @Param("otherUserId") UUID otherUserId);

    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    List<ChatParticipant> findByUser(User user);
}
