package lazyteam.cooking_hansu.domain.chat.dto;

import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.chat.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatFileUploadResDto {
    private List<FileInfo> files; // 업로드된 파일 정보들
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class FileInfo {
        private UUID fileId; // 파일 ID
        private String fileUrl; // S3 URL
        private String fileName; // 파일명
        private FileType fileType; // 파일 타입
        private Integer fileSize; // 파일 크기
    }
} 