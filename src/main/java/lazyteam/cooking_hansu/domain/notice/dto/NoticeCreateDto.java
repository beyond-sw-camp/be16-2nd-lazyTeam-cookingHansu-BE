package lazyteam.cooking_hansu.domain.notice.dto;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NoticeCreateDto {
    @NotEmpty(message = "제목은 필수 입력입니다.")
    private String title;
    @NotEmpty(message = "내용은 필수 입력입니다.")
    private String content;

    private MultipartFile noticeImage; // 공지사항 이미지 파일

    public Notice NoticeToEntity(String imageUrl, Admin admin) {
        return Notice.builder()
                .title(this.title)
                .content(this.content)
                .imageUrl(imageUrl)
                .admin(admin)
                .build();
    }
}
