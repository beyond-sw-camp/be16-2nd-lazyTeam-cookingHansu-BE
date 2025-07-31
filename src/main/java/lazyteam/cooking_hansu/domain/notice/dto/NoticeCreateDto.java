package lazyteam.cooking_hansu.domain.notice.dto;

import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NoticeCreateDto {
    @NotEmpty(message = "제목은 필수 입력입니다.")
    private String title;
    @NotEmpty(message = "내용은 필수 입력입니다.")
    private String content;

    private String imageUrl; // 이미지 URL을 추가할 수 있습니다.

    public Notice NoticeToEntity() {
        return Notice.builder()
                .title(this.title)
                .content(this.content)
                .imageUrl(this.imageUrl)
                .admin(null) // 관리자 정보는 나중에 설정할 수 있습니다.
                .build();
    }
}
