package lazyteam.cooking_hansu.domain.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
import lazyteam.cooking_hansu.domain.chat.entity.ReadStatus;
import lazyteam.cooking_hansu.domain.chat.repository.ChatMessageRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatParticipantRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatRoomRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ReadStatusRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final ChatParticipantRepository chatParticipantRepository;

//    채팅방 생성 or 기존 채팅방 조회
    public UUID getOrCreateChatRoom(UUID otherUserId) {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        User otherUser = userRepository.findById(otherUserId).orElseThrow(() -> new EntityNotFoundException("상대 사용자를 찾을 수 없습니다."));

        // 채팅방이 이미 존재하는지 확인
        Optional<ChatRoom> exsistingChatRoom = chatParticipantRepository.findExsistingChatRoom(user.getId(), otherUser.getId());
        if(exsistingChatRoom.isPresent()){
            return exsistingChatRoom.get().getId();
        }

        ChatRoom newRoom = ChatRoom.builder()
                .name(user.getName() + "과 " + otherUser.getName() + "의 채팅방")
                .build();
        chatRoomRepository.save(newRoom);

        // 채팅방 참여자 추가
        addParticipantToRoom(newRoom, user);
        addParticipantToRoom(newRoom, otherUser);

        return newRoom.getId();
    }

    // 채팅방 참여자 추가
    public void addParticipantToRoom(ChatRoom chatRoom, User user) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    //    메시지 읽음
    public void messageRead(UUID roomId) {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndUser(chatRoom, user);
        for (ReadStatus r : readStatuses) {
            r.updateIsRead("Y");
        }
    }

//    채팅방 나가기
    public void leaveChatRoom(UUID roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(() -> new EntityNotFoundException("채팅방 참여자를 찾을 수 없습니다."));
        chatParticipantRepository.delete(chatParticipant);

        // 채팅방에 참여자가 없으면 채팅방 삭제
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        if(participants.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        }
    }
}
