package lazyteam.cooking_hansu.domain.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.chat.dto.*;
import lazyteam.cooking_hansu.domain.chat.entity.*;
import lazyteam.cooking_hansu.domain.chat.repository.ChatMessageRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatParticipantRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatRoomRepository;
import lazyteam.cooking_hansu.domain.chat.util.ChatFileValidator;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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
@Slf4j
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final S3Uploader s3Uploader;

    //    메세지 전송
    public ChatMessageResDto saveMessage(UUID roomId, ChatMessageReqDto chatMessageReqDto) {
        ChatFileValidator.validateMessageAndFile(chatMessageReqDto);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        User sender = userRepository.findById(chatMessageReqDto.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("발신자를 찾을 수 없습니다."));

        if (!chatParticipantRepository.existsByChatRoomAndUser(chatRoom, sender)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageText(chatMessageReqDto.getMessage())
                .isDeleted("N")
                .build();

        // 파일 DTO가 있으면 엔티티 생성 후 add
        if (chatMessageReqDto.getFiles() != null && !chatMessageReqDto.getFiles().isEmpty()) {
            List<ChatFile> files = chatMessageReqDto.getFiles().stream()
                    .map(f -> ChatFile.builder()
                            .chatMessage(chatMessage)
                            .fileUrl(f.getFileUrl())
                            .fileName(f.getFileName())
                            .fileType(f.getFileType())
                            .fileSize(f.getFileSize())
                            .build())
                    .collect(Collectors.toList());
            chatMessage.getFiles().addAll(files);
        }

        chatMessageRepository.save(chatMessage); // cascade 설정에 따라 files도 함께 저장

        // 채팅방 참여자 중 발신자가 아닌 사람들은 비활성화 상태를 Y로 변경
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant p : participants) {
            if (!p.getUser().getId().equals(sender.getId()) && "N".equals(p.getIsActive())) {
                p.joinChatRoom();
            }
        }

//        // 읽음 상태 – 보낸 사람은 Y, 나머지 N
//        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
//        for (ChatParticipant p : participants) {
//            String readFlag = p.getUser().getId().equals(sender.getId()) ? "Y" : "N";
//            readStatusRepository.save(ReadStatus.builder()
//                    .chatRoom(chatRoom)
//                    .user(p.getUser())
//                    .chatMessage(chatMessage)
//                    .isRead(readFlag)
//                    .build());
//            // 비활성 복구
//            if (!p.getUser().getId().equals(sender.getId()) && "N".equals(p.getIsActive())) {
//                p.joinChatRoom();
//            }
//        }


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
                .createdAt(chatMessage.getCreatedAt())
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
    public PaginatedResponseDto<ChatRoomListDto> getMyChatRooms(int size, String cursor) {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 인덱스 기반 cursor pagination
        int pageIndex = 0;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                pageIndex = Integer.parseInt(cursor);
            } catch (NumberFormatException e) {
                // cursor가 유효한 숫자가 아닌 경우 첫 페이지로
                pageIndex = 0;
            }
        }
        
        // Pageable 생성
        PageRequest pageRequest = PageRequest.of(pageIndex, size);
        
        // Slice로 채팅방 참여자 조회
        Slice<ChatParticipant> chatParticipantsSlice = chatParticipantRepository.findMyActiveParticipantsOrderByLastMessageSlice(user, pageRequest);
        
        // ChatParticipant를 ChatRoomListDto로 변환
        List<ChatRoomListDto> result = chatParticipantsSlice.getContent().stream().map(participant -> {
            User otherUser = chatParticipantRepository.findByChatRoom(participant.getChatRoom()).stream()
                    .map(ChatParticipant::getUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("채팅방에 참여한 다른 사용자를 찾을 수 없습니다."));

            // 가독성 위해 유틸 변수
            ChatRoom room = participant.getChatRoom();

            // 1) 기준점(baseline) 계산: 마지막 읽음 > 나간시각 > 방생성시각
            LocalDateTime baseline =
                    participant.getLastReadMessage() != null ? participant.getLastReadMessage().getCreatedAt() :
                            participant.getLeftAt() != null ? participant.getLeftAt() :
                                    room.getCreatedAt();

            // 2) 필터링: 삭제 제외 + 본인메시지 제외 + baseline 이후
            int newMessageCount = (int) room.getMessages().stream()
                    .filter(m -> "N".equals(m.getIsDeleted()))
                    .filter(m -> !m.getSender().getId().equals(user.getId()))
                    .filter(m -> {
                        // == 같은 시각 메시지까지 읽은 것으로 처리하고 싶으면 isAfter 대신 !isBefore 사용
                        return m.getCreatedAt().isAfter(baseline); // 필요시 >= 로 바꾸려면 !isBefore(baseline)
                    })
                    .count();

            return ChatRoomListDto.fromEntity(room, otherUser, newMessageCount);
        }).collect(Collectors.toList());
        
        // 커스텀 응답 DTO 반환
        return PaginatedResponseDto.<ChatRoomListDto>builder()
                .data(result)
                .hasNext(chatParticipantsSlice.hasNext())
                .nextCursor(chatParticipantsSlice.hasNext() ? String.valueOf(pageIndex + 1) : null)
                .build();
    }

    //    채팅방 상세 내역 조회
    @Transactional(readOnly = true)
    public PaginatedResponseDto<ChatMessageResDto> getChatHistory(UUID roomId, int size, String cursor) {
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

        LocalDateTime lastMessageTime = participant.getLastReadMessage() != null ?
                participant.getLastReadMessage().getCreatedAt() : participant.getCreatedAt();

        // 인덱스 기반 cursor pagination
        int pageIndex = 0;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                pageIndex = Integer.parseInt(cursor);
            } catch (NumberFormatException e) {
                // cursor가 유효한 숫자가 아닌 경우 첫 페이지로
                pageIndex = 0;
            }
        }

        PageRequest pageRequest = PageRequest.of(pageIndex, size);
        
        Slice<ChatMessage> chatMessagesSlice;
        
        if (participant.getLeftAt() != null) {
            // 채팅방을 나갔다가 다시 들어온 경우, 나간 시간 이후 메시지만 조회
            // 이 경우는 기존 List 방식 사용 (pagination 적용 어려움)
            List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomAndCreatedAtAfterOrderByCreatedAtAsc(
                chatRoom, participant.getLeftAt());
            chatMessagesSlice = new SliceImpl<>(chatMessages, pageRequest, false);
        } else {
            // 인덱스 기반 pagination 사용
            chatMessagesSlice = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageRequest);
        }
        
        // Slice를 ChatMessageResDto로 변환
        List<ChatMessageResDto> result = new ArrayList<>();
        for (ChatMessage cm : chatMessagesSlice.getContent()) {
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


        
        // 커스텀 응답 DTO 반환
        return PaginatedResponseDto.<ChatMessageResDto>builder()
                .data(result)
                .hasNext(chatMessagesSlice.hasNext())
                .nextCursor(chatMessagesSlice.hasNext() ? String.valueOf(pageIndex + 1) : null)
                .lastMessageTimestamp(lastMessageTime)
                .build();
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
// 기존: 리스트 조회 후 루프
    public boolean isRoomParticipant(UUID userId, UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        return chatParticipantRepository.existsByChatRoomAndUser(chatRoom, user);
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

    //    메시지 읽음 처리
    public void readMessages(UUID roomId, UUID userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(() -> new EntityNotFoundException("participant not found"));

        chatParticipant.read(chatRoom.getMessages().get(chatRoom.getMessages().size() - 1));
    }
}