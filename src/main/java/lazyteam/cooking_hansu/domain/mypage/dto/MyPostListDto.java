package lazyteam.cooking_hansu.domain.mypage.dto;

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
    private Long bookmarkCount;
    private Long commentCount;
    private Boolean isOpen;
}
