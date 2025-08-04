package lazyteam.cooking_hansu.domain.chat.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageResDto {
    private UUID id; // 메시지 ID
    private UUID roomId; // 채팅방 ID
    private UUID senderId; // 발신자 ID
    @Size(max = 1000, message = "메시지 내용은 1000자 이하여야 합니다")
    private String message; // 메시지 내용
    private List<ChatFileResDto> files; // 파일 목록 (S3 URL 포함)
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime updatedAt; // 수정 시간
}
