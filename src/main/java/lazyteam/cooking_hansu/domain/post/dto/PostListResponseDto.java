package lazyteam.cooking_hansu.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PostListResponseDto {

    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private CategoryEnum category;
    private LevelEnum level;
    private Integer cookTime;
    private Integer serving;
    
    // 카운트 정보
    private Long likeCount;
    private Long viewCount;
    private Long bookmarkCount;
    
    // 작성자 정보
    private String authorNickname;
    private String authorRole;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Post Entity를 PostListResponseDto로 변환
     * 강의쪽 fromEntity 패턴을 참고
     */
    public static PostListResponseDto fromEntity(Post post) {
        return PostListResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .thumbnailUrl(post.getThumbnailUrl())
                .category(post.getCategory())
                .level(post.getLevel())
                .cookTime(post.getCookTime())
                .serving(post.getServing())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .bookmarkCount(post.getBookmarkCount())
                .authorNickname(post.getUser().getNickname())
                .authorRole(post.getUser().getRole().name())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
