package lazyteam.cooking_hansu.domain.chat.dto;

import lazyteam.cooking_hansu.domain.chat.entity.FileType;
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
public class ChatFileUploadReqDto {
    private Long roomId; // 채팅방 ID
    private List<MultipartFile> files; // 업로드할 파일들 (최대 10개)
    private List<FileType> fileTypes; // 파일 타입들
} 