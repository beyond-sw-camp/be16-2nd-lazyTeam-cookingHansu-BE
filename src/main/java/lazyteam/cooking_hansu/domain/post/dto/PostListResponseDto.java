package lazyteam.cooking_hansu.domain.post.dto;

import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.enums.LevelEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
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
    //    상태조회
    private Boolean isLiked;        // 현재 사용자의 좋아요 상태
    private Boolean isBookmarked;   // 현재 사용자의 북마크 상태
    // 카운트 정보
    private Long commentCount;
    private Long likeCount;
    private Long viewCount;
    private Long bookmarkCount;

    // 작성자 정보
    private String nickname;
    private Role role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Post Entity를 PostListResponseDto로 변환
     * 강의쪽 fromEntity 패턴을 참고
     */
    public static PostListResponseDto fromEntity(Post post, Boolean isLiked, Boolean isBookmarked,
                                                 Long commentCount) {
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
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .nickname(post.getUser().getNickname())
                .role(post.getUser().getRole())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentCount(commentCount)
                .build();
    }
}
