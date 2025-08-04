package lazyteam.cooking_hansu.domain.chat.dto;

import lazyteam.cooking_hansu.domain.chat.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatFileResDto {
    private UUID id; // 파일 ID
    private String fileUrl; // S3 URL
    private String fileName; // 파일명
    private FileType fileType; // 파일 타입
    private Integer fileSize; // 파일 크기
    private LocalDateTime createdAt; // 생성 시간
}