package lazyteam.cooking_hansu.domain.chat.controller;

import lazyteam.cooking_hansu.domain.chat.service.ChatService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    //    내 채팅방 목록 조회
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms() {
        var chatRooms = chatService.getMyChatRooms();
        return new ResponseEntity<>(ResponseDto.ok(chatRooms, HttpStatus.OK), HttpStatus.OK);
    }

//    채팅방 상세 메시지 조회
    @GetMapping("/room/{roomId}/history")
    public ResponseEntity<?> getChatHistory(@PathVariable UUID roomId) {
        return null;
    }

//    채팅방 메시지 읽음 처리
    @GetMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable UUID roomId) {
        chatService.messageRead(roomId);
        return new ResponseEntity<>(ResponseDto.ok("메시지가 읽음 처리되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

//    채팅방 참여
    @GetMapping("/room/{roomId}/join")
    public ResponseEntity<?> joinChatRoom(@PathVariable UUID roomId) {
        return null;
    }

//    채팅방 생성
    @GetMapping("/room/create/{otherUserId}")
    public ResponseEntity<?> getOrCreateChatRoom(@PathVariable UUID otherUserId) {
        UUID roomId = chatService.getOrCreateChatRoom(otherUserId);
        return new ResponseEntity<>(ResponseDto.ok(roomId, HttpStatus.CREATED), HttpStatus.CREATED);
    }

//    채팅방 나가기
    @DeleteMapping("/room/{roomId}/leave")
    public ResponseEntity<?> leaveChatRoom(@PathVariable UUID roomId) {
        chatService.leaveChatRoom(roomId);
        return new ResponseEntity<>(ResponseDto.ok("채팅방에서 나갔습니다.", HttpStatus.OK), HttpStatus.OK);
    }


}
