package lazyteam.cooking_hansu.domain.post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "post")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Post extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "게시글 제목은 필수입니다")
    @Size(max = 255, message = "게시글 제목은 255자 이하여야 합니다")
    @Column(nullable = false, length = 255)
    private String title;

    @Size(max = 2000, message = "게시글 설명은 2000자 이하여야 합니다")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "게시글 카테고리는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryEnum category;

    @Size(max = 512, message = "썸네일 URL은 512자 이하여야 합니다")
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Builder.Default
    @Min(value = 0, message = "좋아요 수는 0 이상이어야 합니다")
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Builder.Default
    @Min(value = 0, message = "조회수는 0 이상이어야 합니다")
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Builder.Default
    @Min(value = 0, message = "북마크 수는 0 이상이어야 합니다")
    @Column(name = "bookmark_count", nullable = false)
    private Long bookmarkCount = 0L;

    @NotNull(message = "조리 시간은 필수입니다")
    @Column(name = "cook_time", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer cookTime;

    @NotNull(message = "난이도는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private LevelEnum level;

    @NotNull(message = "인분 수는 필수입니다")
    @Min(value = 1, message = "인분 수는 1 이상이어야 합니다")
    @Max(value = 20, message = "인분 수는 20 이하여야 합니다")
    @Column(name = "serving", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer serving;

    @Size(max = 2000, message = "요리 팁은 2000자 이하여야 합니다")
    @Column(name = "cook_tip", columnDefinition = "TEXT")
    private String cookTip;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredients> ingredients;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeStep> steps;

    @Builder.Default
    @NotNull(message = "공개 여부는 필수입니다")
    @Column(name = "is_open", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isOpen = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


//    레시피 공유 삭제
    public void softDelete() { this.deletedAt = LocalDateTime.now();}

//   삭제 여부 확인
    public boolean isDeleted() { return this.deletedAt != null;}

    public boolean isPublic() {
        return this.isOpen != null && this.isOpen;
    }

    public void updatePost(String title, String description, String thumbnailUrl, 
                          CategoryEnum category, LevelEnum level, Integer cookTime, 
                          Integer serving, String cookTip, Boolean isOpen) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl.trim();
        }
        if (category != null) {
            this.category = category;
        }
        if (level != null) {
            this.level = level;
        }
        if (cookTime != null && cookTime > 0) {
            this.cookTime = cookTime;
        }
        if (serving != null && serving > 0) {
            this.serving = serving;
        }
        if (cookTip != null) {
            this.cookTip = cookTip.trim();
        }
        if (isOpen != null) {
            this.isOpen = isOpen;
        }
    }

    // 소유자 확인 (RecipeService 스타일과 통일)
    public boolean isOwnedBy(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }

    // Redis 동기화를 위한 카운트 업데이트 메서드들
    public void updateViewCount(Long viewCount) {
        this.viewCount = viewCount != null ? viewCount : 0L;
    }

    public void updateLikeCount(Long likeCount) {
        this.likeCount = likeCount != null ? likeCount : 0L;
    }

    public void updateBookmarkCount(Long bookmarkCount) {
        this.bookmarkCount = bookmarkCount != null ? bookmarkCount : 0L;
    }

    public void setViewCount(Long viewCount) {
        updateViewCount(viewCount);
    }

    public void setLikeCount(Long likeCount) {
        updateLikeCount(likeCount);
    }

    public void setBookmarkCount(Long bookmarkCount) {
        updateBookmarkCount(bookmarkCount);
    }
}
