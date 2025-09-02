package lazyteam.cooking_hansu.domain.mypage.dto;

import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MyPostListDto {
    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private Long likeCount;
    private CategoryEnum category;     // 카테고리
    private Long bookmarkCount;
    private Long commentCount;
    private Boolean isOpen;
}
