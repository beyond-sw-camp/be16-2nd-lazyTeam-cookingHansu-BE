package lazyteam.cooking_hansu.domain.post.dto;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 레시피 공유 게시글 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {

    private UUID id;
    private String title;
    private String description;
    private CategoryEnum category;
    private String thumbnailUrl;
    private Integer likeCount;
    private Integer viewCount;
    private Integer bookmarkCount;
    private Boolean isOpen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 작성자 정보
    private UserInfoDto user;
    
    // 연결된 레시피 정보 (있는 경우)
    private RecipeInfoDto recipe;

    /**
     * Entity -> DTO 변환
     */
    public static PostResponseDto fromEntity(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .category(post.getCategory())
                .thumbnailUrl(post.getThumbnailUrl())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .bookmarkCount(post.getBookmarkCount())
                .isOpen(post.getIsOpen())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .user(UserInfoDto.fromEntity(post.getUser()))
                .build();
    }

    /**
     * 사용자 정보 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoDto {
        private UUID id;
        private String nickname;
        private String profileImageUrl;

        public static UserInfoDto fromEntity(lazyteam.cooking_hansu.domain.user.entity.common.User user) {
            return UserInfoDto.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }

    /**
     * 레시피 정보 DTO (간단 버전)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeInfoDto {
        private UUID id;
        private String title;
        private String thumbnailUrl;
        private Integer cookTime;
        private String level;
        private String category;

        public static RecipeInfoDto fromEntity(lazyteam.cooking_hansu.domain.recipe.entity.Recipe recipe) {
            return RecipeInfoDto.builder()
                    .id(recipe.getId())
                    .title(recipe.getTitle())
                    .thumbnailUrl(recipe.getThumbnailUrl())
                    .cookTime(recipe.getCookTime())
                    .level(recipe.getLevel().name())
                    .category(recipe.getCategory().name())
                    .build();
        }
    }

    /**
     * 게시글 요약 정보 (목록용)
     */
    public static PostResponseDto summaryFromEntity(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription() != null && post.getDescription().length() > 100 
                    ? post.getDescription().substring(0, 100) + "..." 
                    : post.getDescription())
                .category(post.getCategory())
                .thumbnailUrl(post.getThumbnailUrl())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .bookmarkCount(post.getBookmarkCount())
                .isOpen(post.getIsOpen())
                .createdAt(post.getCreatedAt())
                .user(UserInfoDto.fromEntity(post.getUser()))
                .build();
    }
}
