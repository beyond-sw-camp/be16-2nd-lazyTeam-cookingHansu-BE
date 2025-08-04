package lazyteam.cooking_hansu.domain.chat.dto;

import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageDto extends BaseIdAndTimeEntity {
    private UUID senderId; // 발신자 ID
    @Size(max = 1000, message = "메시지 내용은 1000자 이하여야 합니다")
    private String message; // 메시지 내용
    private List<MultiImages> file; // 업로드할 파일 목록
}
