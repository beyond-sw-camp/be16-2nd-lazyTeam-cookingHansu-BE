package lazyteam.cooking_hansu.domain.notice.dto;


import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NoticeDetailDto {
    private Long id;
    private String title;
    private String content;
    private String imageUrl; // 이미지 URL을 추가할 수 있습니다.
    private String authorName; // 작성자 이름

    public static NoticeDetailDto fromEntity(Notice notice) {
        return NoticeDetailDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrl(notice.getImageUrl())
                .authorName(notice.getWriter())
                .build();
    }

}
