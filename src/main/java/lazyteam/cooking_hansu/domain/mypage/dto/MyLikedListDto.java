package lazyteam.cooking_hansu.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MyLikedListDto {
    private String title;
    private String thumbnailUrl;
    private String description;
    private String writerNickname;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer bookmarkCount;

}
