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
    
    // 카운트 정보
    private Long likeCount;
    private Long viewCount;
    private Long bookmarkCount;

//    이넘
    // 작성자 정보
    private String nickname;
    private Role role;

    private LocalDateTime createdAt;

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
                .nickname(post.getUser().getNickname())
                .role(post.getUser().getRole())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
