//package lazyteam.cooking_hansu.domain.chat.repository;
//
//import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
//import lazyteam.cooking_hansu.domain.chat.entity.ReadStatus;
//import lazyteam.cooking_hansu.domain.user.entity.common.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.UUID;
//
//@Repository
//public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
//
//    @Modifying(clearAutomatically = true, flushAutomatically = true)
//    @Query("""
//              update ReadStatus r
//                 set r.isRead = 'Y'
//               where r.chatRoom.id = :roomId
//                 and r.user.id = :userId
//                 and r.isRead = 'N'
//            """)
//    int markAllRead(UUID roomId, UUID userId);
//
//    Long countByChatRoomAndUserAndIsRead(ChatRoom chatRoom, User user, String isRead);
//}
