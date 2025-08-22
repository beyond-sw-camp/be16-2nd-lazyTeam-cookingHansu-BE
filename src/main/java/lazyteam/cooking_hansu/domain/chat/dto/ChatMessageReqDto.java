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
public class ChatMessageReqDto {
    private Long roomId; // 채팅방 ID
    private UUID senderId; // 발신자 ID
    @Size(max = 1000, message = "메시지 내용은 1000자 이하여야 합니다")
    private String message; // 메시지 내용
    private List<ChatFileUploadResDto.FileInfo> files; // 업로드할 파일 목록 (MultipartFile 포함)
    private LocalDateTime createdAt; // 생성 시간
} 