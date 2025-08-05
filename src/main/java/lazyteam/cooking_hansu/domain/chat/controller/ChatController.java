package lazyteam.cooking_hansu.domain.chat.controller;

import lazyteam.cooking_hansu.domain.chat.dto.ChatFileUploadReqDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatFileUploadResDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageResDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatRoomUpdateDto;
import lazyteam.cooking_hansu.domain.chat.dto.MyChatListDto;
import lazyteam.cooking_hansu.domain.chat.service.ChatService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    //    내 채팅방 목록 조회
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms() {
        List<MyChatListDto> myChatRooms = chatService.getMyChatRooms();
        return new ResponseEntity<>(ResponseDto.ok(myChatRooms, HttpStatus.OK), HttpStatus.OK);
    }

    //    채팅방 상세 메시지 조회
    @GetMapping("/room/{roomId}/history")
    public ResponseEntity<?> getChatHistory(@PathVariable UUID roomId) {
        List<ChatMessageResDto> chatHistory = chatService.getChatHistory(roomId);
        return new ResponseEntity<>(ResponseDto.ok(chatHistory, HttpStatus.OK), HttpStatus.OK);
    }

    //    채팅방 메시지 읽음 처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable UUID roomId) {
        chatService.messageRead(roomId);
        return new ResponseEntity<>(ResponseDto.ok("메시지가 읽음 처리되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

    //    채팅방 생성
    @GetMapping("/room/create/{otherUserId}")
    public ResponseEntity<?> getOrCreateChatRoom(@PathVariable UUID otherUserId) {
        UUID roomId = chatService.getOrCreateChatRoom(otherUserId);
        return new ResponseEntity<>(ResponseDto.ok(roomId, HttpStatus.CREATED), HttpStatus.CREATED);
    }

    //    채팅방 이름 수정
    @PatchMapping("/room/{roomId}/name")
    public ResponseEntity<?> updateChatRoomName(@PathVariable UUID roomId, @RequestBody ChatRoomUpdateDto updateDto) {
        chatService.updateChatRoomName(roomId, updateDto);
        return new ResponseEntity<>(ResponseDto.ok("채팅방 이름이 변경되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

    //    채팅방 나가기
    @DeleteMapping("/room/{roomId}/leave")
    public ResponseEntity<?> leaveChatRoom(@PathVariable UUID roomId) {
        chatService.leaveChatRoom(roomId);
        return new ResponseEntity<>(ResponseDto.ok("채팅방에서 나갔습니다.", HttpStatus.OK), HttpStatus.OK);
    }

    //    파일 업로드 API (최대 10개)
    @PostMapping("/room/{roomId}/upload")
    public ResponseEntity<?> uploadFiles(@PathVariable UUID roomId, @ModelAttribute ChatFileUploadReqDto requestDto) {
        ChatFileUploadResDto result = chatService.uploadFiles(roomId, requestDto);
        return new ResponseEntity<>(ResponseDto.ok(result, HttpStatus.OK), HttpStatus.OK);
    }
}

