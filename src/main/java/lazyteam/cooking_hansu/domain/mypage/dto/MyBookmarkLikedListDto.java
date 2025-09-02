package lazyteam.cooking_hansu.domain.mypage.dto;

import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class MyBookmarkLikedListDto {
    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private CategoryEnum category;     // 카테고리
    private Long likeCount;
    private Long bookmarkCount;
    private LocalDateTime createdAt;
    private String writerNickname;
    private Long commentCount;
    private Boolean isOpen;
}

