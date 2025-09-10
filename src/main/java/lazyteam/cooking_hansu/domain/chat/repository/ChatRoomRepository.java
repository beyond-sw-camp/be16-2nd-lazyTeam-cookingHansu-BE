package lazyteam.cooking_hansu.domain.chat.repository;

import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
