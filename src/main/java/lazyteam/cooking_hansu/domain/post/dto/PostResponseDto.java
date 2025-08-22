package lazyteam.cooking_hansu.domain.post.dto;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
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
    private Long likeCount;
    private Long viewCount;
    private Long bookmarkCount;
    private Boolean isOpen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 작성자 정보
    private UserInfoDto user;
    
    // 연결된 레시피 정보 (있는 경우)
    private RecipeInfoDto recipe;
    
    // 레시피 단계별 설명 (있는 경우)
    private List<StepDescriptionDto> stepDescriptions;

    /**
     * 레시피 단계별 설명 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepDescriptionDto {
        private UUID stepId;
        private Integer stepSequence;
        private String originalContent;     // 원본 레시피의 조리순서 내용
        private String additionalContent;   // 게시글 작성자가 추가한 설명
        
        public static StepDescriptionDto fromEntity(lazyteam.cooking_hansu.domain.recipe.entity.PostSequenceDescription description) {
            return StepDescriptionDto.builder()
                    .stepId(description.getRecipeStep().getId())
                    .stepSequence(description.getRecipeStep().getStepSequence())
                    .originalContent(description.getRecipeStep().getContent())
                    .additionalContent(description.getContent())
                    .build();
        }
    }

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
     * Entity -> DTO 변환 (레시피 연결 정보 포함)
     */
    public static PostResponseDto fromEntityWithRecipe(Post post, 
                                                       lazyteam.cooking_hansu.domain.recipe.entity.Recipe recipe, 
                                                       List<lazyteam.cooking_hansu.domain.recipe.entity.PostSequenceDescription> descriptions) {
        PostResponseDto dto = fromEntity(post);
        
        if (recipe != null) {
            dto.setRecipe(RecipeInfoDto.fromEntity(recipe));
        }
        
        if (descriptions != null && !descriptions.isEmpty()) {
            List<StepDescriptionDto> stepDescriptions = descriptions.stream()
                    .map(StepDescriptionDto::fromEntity)
                    .toList();
            dto.setStepDescriptions(stepDescriptions);
        }
        
        return dto;
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
        private String role; // 유저 역할 정보 추가

        public static UserInfoDto fromEntity(lazyteam.cooking_hansu.domain.user.entity.common.User user) {
            return UserInfoDto.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .role(user.getRole() != null ? user.getRole().name() : "GENERAL")
                    .build();
        }
        
        /**
         * 역할을 한국어로 변환
         */
        public String getRoleKorean() {
            if (role == null) return "일반 사용자";
            return switch (role) {
                case "GENERAL" -> "일반 사용자";
                case "CHEF" -> "셰프";
                case "OWNER" -> "자영업자";
                default -> "일반 사용자";
            };
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
        // ========== 인분 정보: 숫자만 저장 ==========
        private Integer servings;        // 몇 인분 (숫자만)

        public static RecipeInfoDto fromEntity(lazyteam.cooking_hansu.domain.recipe.entity.Recipe recipe) {
            return RecipeInfoDto.builder()
                    .id(recipe.getId())
                    .title(recipe.getTitle())
                    .thumbnailUrl(recipe.getThumbnailUrl())
                    .cookTime(recipe.getCookTime())
                    .level(recipe.getLevel().name())
                    .category(recipe.getCategory().name())
                    .servings(recipe.getServings())                    // ← 숫자만 저장
                    .build();
        }
        
        /**
         * 인분을 텍스트로 변환하는 헬퍼 메서드 (프론트엔드에서 사용)
         * null인 경우 null 반환
         */
        public String getServingsText() {
            return this.servings != null ? this.servings + "인분" : null;
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
