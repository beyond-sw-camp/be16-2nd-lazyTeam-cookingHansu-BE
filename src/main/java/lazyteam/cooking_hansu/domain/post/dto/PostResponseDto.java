package lazyteam.cooking_hansu.domain.post.dto;

import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.enums.LevelEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.entity.Ingredients;
import lazyteam.cooking_hansu.domain.post.entity.RecipeStep;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {

    private UUID id;
    private String title;
    private String description;
    private CategoryEnum category;
    private LevelEnum level;
    private Integer cookTime;
    private Integer serving;
    private String cookTip;
    private String thumbnailUrl;
    private Long likeCount;
    private Long viewCount;
    private Long bookmarkCount;
    private Boolean isLiked;        // 좋아요 눌렀는지 확인하는거 추가
    private Boolean isBookmarked;   // 북마크 눌렀는지 확인하는거 추가
    private Boolean isOpen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserInfoDto user;
    private List<IngredientDto> ingredients;
    private List<RecipeStepDto> steps;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngredientDto {
        private UUID id;
        private String name;
        private String amount;

        public static IngredientDto fromEntity(Ingredients ingredient) {
            if (ingredient == null) return null;
            return IngredientDto.builder()
                    .id(ingredient.getId())
                    .name(ingredient.getName())
                    .amount(ingredient.getAmount())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeStepDto {
        private UUID id;
        private Integer stepSequence;
        private String content;
        private String description;

        public static RecipeStepDto fromEntity(RecipeStep step) {
            if (step == null) return null;
            return RecipeStepDto.builder()
                    .id(step.getId())
                    .stepSequence(step.getStepSequence())
                    .content(step.getContent())
                    .description(step.getDescription())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoDto {
        private UUID id;
        private String nickname;
        private String profileImageUrl;
        private Role role;

        public static UserInfoDto fromEntity(User user) {
            return UserInfoDto.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getPicture())
                    .role(user.getRole())
                    .build();
        }
    }

    // 통합 Post 엔티티로부터 DTO 생성 (재료, 조리순서 포함)
    public static PostResponseDto fromEntity(Post post, List<Ingredients> ingredients, List<RecipeStep> steps,
                                             Boolean isLiked, Boolean isBookmarked) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .category(post.getCategory())
                .level(post.getLevel())
                .cookTime(post.getCookTime())
                .serving(post.getServing())
                .cookTip(post.getCookTip())
                .thumbnailUrl(post.getThumbnailUrl())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .bookmarkCount(post.getBookmarkCount())
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .isOpen(post.getIsOpen())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .user(UserInfoDto.fromEntity(post.getUser()))
                .ingredients(ingredients != null ? ingredients.stream().map(IngredientDto::fromEntity).toList() : List.of())
                .steps(steps != null ? steps.stream().map(RecipeStepDto::fromEntity).toList() : List.of())
                .build();
    }
}