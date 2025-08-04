package lazyteam.cooking_hansu.domain.chat.dto;

import lazyteam.cooking_hansu.domain.chat.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MultiImages {
    private MultipartFile file; // 업로드할 파일 (이미지 또는 비디오)
    private FileType fileType; // 파일 타입 IMAGE, VIDEO
}
