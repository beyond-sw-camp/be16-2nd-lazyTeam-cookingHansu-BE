package lazyteam.cooking_hansu.domain.notice.dto;

import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NoticeListDto {
    private Long id;
    private String title;
    private String content;
    private String imageUrl; // 이미지 URL을 추가할 수 있습니다.
    private String authorName; // 작성자 이름

    public static NoticeListDto fromEntity(Notice notice) {
        return new NoticeListDto(
//                notice.getId(),
//                notice.getTitle(),
//                notice.getContent(),
//                notice.getImageUrl(),
//                notice.getWriter()
        );
    }
}
