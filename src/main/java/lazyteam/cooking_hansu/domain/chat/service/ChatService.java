package lazyteam.cooking_hansu.domain.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageTextDto;
import lazyteam.cooking_hansu.domain.chat.dto.MyChatListDto;
import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
import lazyteam.cooking_hansu.domain.chat.entity.ChatMessage;
import lazyteam.cooking_hansu.domain.chat.entity.ReadStatus;
import lazyteam.cooking_hansu.domain.chat.repository.ChatMessageRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatParticipantRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatRoomRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ReadStatusRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final ChatParticipantRepository chatParticipantRepository;


//    메세지 전송
    public void saveMessage(UUID roomId, ChatMessageTextDto messageTextDto) {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));

        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .messageText(messageTextDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 읽음 상태 저장
        ReadStatus readStatus = ReadStatus.builder()
                .chatRoom(chatRoom)
                .user(user)
                .isRead("N") // 처음에는 읽지 않은 상태로 설정
                .build();
        readStatusRepository.save(readStatus);
    }

//    내 채팅방 목록 조회
    public List<MyChatListDto> getMyChatRooms() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByUser(user);
        
        return chatParticipants.stream()
                .map(participant -> {
                    ChatRoom chatRoom = participant.getChatRoom();
                    
                    // 상대방 정보 찾기 (현재 사용자가 아닌 다른 참여자)
                    User otherUser = chatParticipantRepository.findByChatRoom(chatRoom).stream()
                            .map(ChatParticipant::getUser)
                            .filter(u -> !u.getId().equals(user.getId()))
                            .findFirst()
                            .orElseThrow(() -> new EntityNotFoundException("채팅방에 참여한 다른 사용자를 찾을 수 없습니다."));

                    // 마지막 메시지 찾기
                    ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom).orElseThrow(() -> new EntityNotFoundException("채팅방에 메시지가 없습니다."));
                    
                    // 읽지 않은 메시지 수 계산
                    Integer unreadCount = readStatusRepository.countByChatRoomAndUserIsReadFalse(chatRoom, user).intValue();
                    
                    return MyChatListDto.builder()
                            .chatRoomId(chatRoom.getId())
                            .chatRoomName(chatRoom.getName())
                            .otherUserName(otherUser.getName())
                            .otherUserNickname(otherUser.getNickname())
                            .otherUserProfileImage(otherUser.getProfileImageUrl())
                            .lastMessage(lastMessage.getMessageText())
                            .lastMessageTime(lastMessage.getCreatedAt())
                            .unreadCount(unreadCount)
                            .build();
                }).collect(Collectors.toList());
    }

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

//        만약 나와 상대방 1:1채팅이 없을경우 채팅방 개설
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

//    방 참여자인지 확인
    public boolean isRoomParticipant(UUID userId, UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant c : chatParticipants) {
            if (c.getUser().equals(user)) {
                return true; // 해당 이메일이 참여자 목록에 있으면 메서드 종료
            }
        }
        return false;
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
