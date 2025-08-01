package lazyteam.cooking_hansu.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoticeUpdateDto {
    @NotEmpty(message = "제목은 필수 입력입니다.")
    private String title; // 공지사항 제목

    @NotEmpty(message = "내용은 필수 입력입니다.")
    private String content; // 공지사항 내용

    private MultipartFile noticeImage; // 이미지 URL (선택 사항)
}
