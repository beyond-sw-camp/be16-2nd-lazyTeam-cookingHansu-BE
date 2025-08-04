package lazyteam.cooking_hansu.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageFileDto {
    private UUID roomId; // 채팅방 ID
    private UUID senderId; // 발신자 ID
    private String fileName; // 파일 이름
    private MultipartFile file; // 업로드된 파일
    private String fileType; // 파일 타입 (예: image/png, application/pdf 등)
}
