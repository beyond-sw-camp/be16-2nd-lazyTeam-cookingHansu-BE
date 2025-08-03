package lazyteam.cooking_hansu.domain.chat.repository;

import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    Optional<ChatRoom> findExsistingChatRoom(UUID userId, UUID otherUserId);

    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    List<ChatParticipant> findByUser(User user);
}
