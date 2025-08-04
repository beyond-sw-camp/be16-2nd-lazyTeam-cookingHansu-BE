package lazyteam.cooking_hansu.domain.mypage.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class MyBookmarkLikedListDto {
    private String title;
    private String description;
    private String thumbnailUrl;
    private int likeCount;
    private int bookmarkCount;
    private LocalDateTime createdAt;
    private String writerNickname;
}

