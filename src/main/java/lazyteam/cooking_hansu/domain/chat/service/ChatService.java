package lazyteam.cooking_hansu.domain.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.chat.dto.*;
import lazyteam.cooking_hansu.domain.chat.entity.*;
import lazyteam.cooking_hansu.domain.chat.repository.ChatMessageRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatParticipantRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatRoomRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ReadStatusRepository;
import lazyteam.cooking_hansu.domain.chat.util.ChatMessageFormatter;
import lazyteam.cooking_hansu.domain.chat.util.ChatFileValidator;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final S3Uploader s3Uploader;


    //    메세지 전송
    public ChatMessageResDto saveMessage(UUID roomId, ChatMessageReqDto chatMessageReqDto) {
        // 유효성 검사
        ChatFileValidator.validateMessageAndFile(chatMessageReqDto);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        User sender = userRepository.findById(chatMessageReqDto.getSenderId()).orElseThrow(() -> new EntityNotFoundException("발신자를 찾을 수 없습니다."));

        // 현재 사용자가 채팅방 참여자인지 확인
        if (!isRoomParticipant(sender.getId(), roomId)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
        // 메시지 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageText(chatMessageReqDto.getMessage())
                .isDeleted("N") // 기본값은 "N" (삭제되지 않음)
                .build();

        chatMessageRepository.save(chatMessage);

        // 파일이 있는 경우 파일 저장
        if(chatMessageReqDto.getFiles() != null && !chatMessageReqDto.getFiles().isEmpty()) {
            List<ChatFile> chatFiles = chatMessageReqDto.getFiles().stream()
                    .map(file -> ChatFile.builder()
                            .chatMessage(chatMessage)
                            .fileUrl(file.getFileUrl())
                            .fileName(file.getFileName())
                            .fileType(file.getFileType())
                            .fileSize(file.getFileSize())
                            .build())
                    .collect(Collectors.toList());
            
            // 파일들을 메시지에 추가
            chatMessage.getFiles().addAll(chatFiles);
        }

        // 메시지 읽음 상태 저장
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant participant : participants) {
            String readFlag = participant.getUser().equals(sender) ? "Y" : "N";
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .user(participant.getUser())
                    .chatMessage(chatMessage)
                    .isRead(readFlag)
                    .build();
            readStatusRepository.save(readStatus);
        }

        // 참여자 상태 복구
        for (ChatParticipant participant : participants) {
            if (!participant.getUser().getId().equals(sender.getId())) {
                if ("N".equals(participant.getIsActive())) {
                    participant.joinChatRoom(); // 참여자 상태 복구: isActive = "Y"
                }
            }
        }

        return ChatMessageResDto.builder()
                .id(chatMessage.getId())
                .roomId(roomId)
                .senderId(sender.getId())
                .message(chatMessage.getMessageText())
                .files(chatMessage.getFiles().stream()
                        .map(file -> ChatFileUploadResDto.FileInfo.builder()
                                .fileId(file.getId())
                                .fileUrl(file.getFileUrl())
                                .fileName(file.getFileName())
                                .fileType(file.getFileType())
                                .fileSize(file.getFileSize())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(chatMessage.getCreatedAt()) // 생성 시간
                .build();
    }

    //    채팅메시지 파일 업로드
    public ChatFileUploadResDto uploadFiles(UUID roomId, ChatFileUploadReqDto requestDto) {
        // 채팅방 존재 확인
        chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        
        // 현재 사용자가 채팅방 참여자인지 확인
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        if (!isRoomParticipant(userId, roomId)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
        
        List<MultipartFile> files = requestDto.getFiles();
        List<FileType> fileTypes = requestDto.getFileTypes();
        
        // 업로드 요청 전체 검증 (파일 개수, 크기, 타입 등 모든 검증)
        ChatFileValidator.validateUploadRequest(files, fileTypes);
        
        List<ChatFileUploadResDto.FileInfo> uploadedFiles = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            FileType fileType = fileTypes.get(i);
            
            // S3에 파일 업로드
            String fileUrl = s3Uploader.upload(file, "chat-files/" + roomId);
            
            // 파일 정보 생성
            ChatFileUploadResDto.FileInfo fileInfo = ChatFileUploadResDto.FileInfo.builder()
                    .fileUrl(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .fileType(fileType)
                    .fileSize((int) file.getSize())
                    .build();
            
            uploadedFiles.add(fileInfo);
        }
        
        return ChatFileUploadResDto.builder()
                .files(uploadedFiles)
                .build();
    }

    //    내 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<MyChatListDto> getMyChatRooms() {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        List<ChatParticipant> chatParticipants =
                chatParticipantRepository.findMyActiveParticipantsOrderByLastMessage(user);

        return chatParticipants.stream()
                .map(participant -> {
                    ChatRoom chatRoom = participant.getChatRoom();

                    // 상대방 찾기 (현 구조 유지)
                    User otherUser = chatParticipantRepository.findByChatRoom(chatRoom).stream()
                            .map(ChatParticipant::getUser)
                            .filter(u -> !u.getId().equals(user.getId()))
                            .findFirst()
                            .orElseThrow(() -> new EntityNotFoundException("채팅방에 참여한 다른 사용자를 찾을 수 없습니다."));

                    // 마지막 메시지(내용/시간)
                    Optional<ChatMessage> lastMessageOpt =
                            chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom);

                    String lastMessage = ChatMessageFormatter.formatLastMessage(lastMessageOpt);
                    LocalDateTime lastMessageTime = lastMessageOpt.map(ChatMessage::getCreatedAt).orElse(null);

                    // 미읽음 카운트
                    int unreadCount = readStatusRepository
                            .countByChatRoomAndUserAndIsRead(chatRoom, user, "N")
                            .intValue();

                    return MyChatListDto.builder()
                            .chatRoomId(chatRoom.getId())
                            .customRoomName(ChatMessageFormatter.formatChatRoomName(
                                    participant.getCustomRoomName(), 
                                    otherUser.getNickname()))
                            .otherUserName(otherUser.getName())
                            .otherUserNickname(otherUser.getNickname())
                            .otherUserProfileImage(otherUser.getProfileImageUrl())
                            .lastMessage(lastMessage)
                            .lastMessageTime(lastMessageTime)
                            .unreadCount(unreadCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    //    채팅방 상세 내역 조회
    @Transactional(readOnly = true)
    public List<ChatMessageResDto> getChatHistory(UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        // 현재 사용자가 채팅방 참여자인지 확인
        boolean check = false;
        for (ChatParticipant c : chatParticipants) {
            if (c.getUser().equals(user)) {
                check = true;
            }
        }
        if (!check) throw new EntityNotFoundException("해당 채팅방에 참여하지 않은 사용자입니다.");

        ChatParticipant participant = chatParticipantRepository
                .findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new EntityNotFoundException("채팅방에 참여한 기록이 없습니다."));

        List<ChatMessage> chatMessages;

        if (participant.getLeftAt() != null) {
            chatMessages = chatMessageRepository.findByChatRoomAndCreatedAtAfterOrderByCreatedAtAsc(chatRoom, participant.getLeftAt());
        } else {
            chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
        }
        List<ChatMessageResDto> result = new ArrayList<>();
        for (ChatMessage cm : chatMessages) {
            // 메시지에 파일이 있는 경우
            List<ChatFileUploadResDto.FileInfo> fileList = new ArrayList<>();
            if (cm.getFiles() != null && !cm.getFiles().isEmpty()) {
                for (ChatFile file : cm.getFiles()) {
                    ChatFileUploadResDto.FileInfo fileInfo = ChatFileUploadResDto.FileInfo.builder()
                            .fileId(file.getId())
                            .fileUrl(file.getFileUrl())
                            .fileName(file.getFileName())
                            .fileType(file.getFileType())
                            .fileSize(file.getFileSize())
                            .build();
                    fileList.add(fileInfo);
                }
            }

            ChatMessageResDto chatMessageResDto = ChatMessageResDto.builder()
                    .id(cm.getId())
                    .roomId(chatRoom.getId())
                    .senderId(cm.getSender().getId())
                    .message(cm.getMessageText())
                    .files(fileList.isEmpty() ? null : fileList)
                    .createdAt(cm.getCreatedAt())
                    .updatedAt(cm.getUpdatedAt())
                    .build();
            result.add(chatMessageResDto);
        }
        return result;
    }

    //    채팅방 생성 or 기존 채팅방 조회
    public UUID getOrCreateChatRoom(UUID otherUserId) {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        User otherUser = userRepository.findById(otherUserId).orElseThrow(() -> new EntityNotFoundException("상대 사용자를 찾을 수 없습니다."));

        // 채팅방이 이미 존재하는지 확인
        Optional<ChatRoom> existingChatRoom = chatParticipantRepository.findExistingChatRoom(user.getId(), otherUser.getId());
        if (existingChatRoom.isPresent()) {
            return existingChatRoom.get().getId();
        }

//        만약 나와 상대방 1:1채팅이 없을경우 채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
                .name(otherUser.getName())
                .build();
        chatRoomRepository.save(newRoom);

        // 채팅방 참여자 추가
        addParticipantToRoom(newRoom, user, otherUser);
        addParticipantToRoom(newRoom, otherUser, user);

        return newRoom.getId();
    }

    // 채팅방 참여자 추가
    public void addParticipantToRoom(ChatRoom chatRoom, User user, User otherUser) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .customRoomName(otherUser.getName())
                .isActive("Y")
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
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndUser(chatRoom, user);
        for (ReadStatus r : readStatuses) {
            r.updateIsRead("Y");
        }
    }

    //    채팅방 나가기
    public void leaveChatRoom(UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(() -> new EntityNotFoundException("채팅방 참여자를 찾을 수 없습니다."));
        chatParticipant.leaveChatRoom();

        // 채팅방에 참여자가 없으면 채팅방 삭제
        long activeCount = chatParticipantRepository.countByChatRoomAndIsActive(chatRoom, "Y");
        if (activeCount == 0) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    //    채팅방 이름 변경(상대방 이름 변경)
    public void updateChatRoomName(UUID roomId, ChatRoomUpdateDto updateDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));

        // 현재 사용자가 채팅방 참여자인지 확인
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (!isRoomParticipant(user.getId(), roomId)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
        // 채팅방 참여자 중 현재 사용자를 찾음
        ChatParticipant participant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(() -> new EntityNotFoundException("채팅방 참여자를 찾을 수 없습니다."));
        // 참여자의 커스텀 채팅방 이름을 업데이트
        participant.updateCustomRoomName(updateDto.getName());

    }
}