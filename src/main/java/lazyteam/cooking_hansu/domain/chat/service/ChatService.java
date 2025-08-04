package lazyteam.cooking_hansu.domain.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.chat.dto.ChatFileReqDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatFileResDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageReqDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageResDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatRoomUpdateDto;
import lazyteam.cooking_hansu.domain.chat.dto.MyChatListDto;
import lazyteam.cooking_hansu.domain.chat.entity.*;
import lazyteam.cooking_hansu.domain.chat.repository.ChatMessageRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatParticipantRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ChatRoomRepository;
import lazyteam.cooking_hansu.domain.chat.repository.ReadStatusRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public void saveMessage(UUID roomId, ChatMessageReqDto chatMessageReqDto) {
        // 유효성 검사
        validateMessageAndFile(chatMessageReqDto);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        User sender = userRepository.findById(chatMessageReqDto.getSenderId()).orElseThrow(() -> new EntityNotFoundException("발신자를 찾을 수 없습니다."));

        ChatMessage chatMessage;
        // 파일이 있는 경우
        if (chatMessageReqDto.getFiles() != null && !chatMessageReqDto.getFiles().isEmpty()) {

            chatMessage = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .messageText(null)
                    .build();
            for (ChatFileReqDto file : chatMessageReqDto.getFiles()) {
                // S3에 파일 업로드
                String fileUrl = s3Uploader.upload(file.getFile(), "chat-files");

                // ChatFile 엔티티 생성
                ChatFile chatFile = ChatFile.builder()
                        .chatMessage(chatMessage)
                        .fileUrl(fileUrl)
                        .fileName(file.getFile().getOriginalFilename())
                        .fileSize((int) file.getFile().getSize())
                        .fileType(file.getFileType())
                        .build();

                // ChatMessage에 파일 추가
                chatMessage.getFiles().add(chatFile);
            }
        }
        // 텍스트 메시지인 경우
        else {
            chatMessage = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .messageText(chatMessageReqDto.getMessage())
                    .build();
        }
        chatMessageRepository.save(chatMessage);

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
    }

    //    내 채팅방 목록 조회
    @Transactional(readOnly = true)
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

    //    채팅방 상세 내역 조회
    @Transactional(readOnly = true)
    public List<ChatMessageResDto> getChatHistory(UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
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

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
        List<ChatMessageResDto> result = new ArrayList<>();
        for (ChatMessage cm : chatMessages) {
            // 메시지에 파일이 있는 경우
            List<ChatFileResDto> fileList = new ArrayList<>();
            if (cm.getFiles() != null && !cm.getFiles().isEmpty()) {
                for (ChatFile file : cm.getFiles()) {
                    ChatFileResDto fileDto = ChatFileResDto.builder()
                            .id(file.getId())
                            .fileUrl(file.getFileUrl())
                            .fileName(file.getFileName())
                            .fileSize(file.getFileSize())
                            .fileType(file.getFileType())
                            .createdAt(file.getCreatedAt())
                            .build();
                    fileList.add(fileDto);
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
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        User otherUser = userRepository.findById(otherUserId).orElseThrow(() -> new EntityNotFoundException("상대 사용자를 찾을 수 없습니다."));

        // 채팅방이 이미 존재하는지 확인
        Optional<ChatRoom> exsistingChatRoom = chatParticipantRepository.findExsistingChatRoom(user.getId(), otherUser.getId());
        if (exsistingChatRoom.isPresent()) {
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
    public void leaveChatRoom(UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(() -> new EntityNotFoundException("채팅방 참여자를 찾을 수 없습니다."));
        chatParticipantRepository.delete(chatParticipant);

        // 채팅방에 참여자가 없으면 채팅방 삭제
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        if (participants.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    //    채팅방 이름 변경
    public void updateChatRoomName(UUID roomId, ChatRoomUpdateDto updateDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));

        // 현재 사용자가 채팅방 참여자인지 확인
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (isRoomParticipant(userId, roomId)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }

        // 채팅방 이름 변경
        chatRoom.updateName(updateDto.getName());
    }

    // 메시지와 파일 유효성 검사
    private void validateMessageAndFile(ChatMessageReqDto chatMessageReqDto) {
        boolean hasMessage = chatMessageReqDto.getMessage() != null && !chatMessageReqDto.getMessage().trim().isEmpty();
        boolean hasFile = chatMessageReqDto.getFiles() != null && !chatMessageReqDto.getFiles().isEmpty();

        // 메시지만 있는 경우
        if (hasMessage && !hasFile) {
            // 메시지는 비어있으면 안됨 (이미 위에서 체크했지만 한번 더)
            if (chatMessageReqDto.getMessage().trim().isEmpty()) {
                throw new IllegalArgumentException("메시지 내용은 비어있을 수 없습니다.");
            }
        }
        // 파일만 있는 경우
        else if (!hasMessage && hasFile) {
            // 파일은 1개 이상 있어야 함
            if (chatMessageReqDto.getFiles().size() < 1) {
                throw new IllegalArgumentException("파일은 최소 1개 이상 있어야 합니다.");
            }

            // 각 파일에 대한 추가 검증
            for (ChatFileReqDto fileReq : chatMessageReqDto.getFiles()) {
                if (fileReq.getFile() == null || fileReq.getFile().isEmpty()) {
                    throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
                }

                // 파일 타입 검증
                validateFileType(fileReq.getFile(), fileReq.getFileType());
            }
        }
        // 둘 다 있는 경우 (허용하지 않음)
        else if (hasMessage && hasFile) {
            throw new IllegalArgumentException("메시지와 파일을 동시에 전송할 수 없습니다. 메시지 또는 파일 중 하나만 전송해주세요.");
        }
        // 둘 다 없는 경우
        else {
            throw new IllegalArgumentException("메시지 또는 파일 중 하나는 반드시 있어야 합니다.");
        }
    }

    // 파일 타입 검증
    private void validateFileType(MultipartFile file, FileType fileType) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        switch (fileType) {
            case IMAGE:
                if (!extension.matches("(jpg|jpeg|png|gif|webp)")) {
                    throw new IllegalArgumentException("이미지 파일은 jpg, jpeg, png, gif 형식만 허용됩니다.");
                }
                break;
            case VIDEO:
                if (!extension.matches("(pdf|doc|docx|txt|zip|rar|7z)")) {
                    throw new IllegalArgumentException("비디오 파일은 mp4, avi, mov 형식만 허용됩니다.");
                }
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 파일 타입입니다.");
        }
    }
}
