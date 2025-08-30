package lazyteam.cooking_hansu.domain.mypage.dto;

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
    private Long likeCount;
    private Long bookmarkCount;
    private LocalDateTime createdAt;
    private String writerNickname;
    private Long commentCount;
    private Boolean isOpen;
}

