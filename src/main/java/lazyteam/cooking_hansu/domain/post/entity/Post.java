package lazyteam.cooking_hansu.domain.post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.time.LocalDateTime;

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

    @Builder.Default
    @NotNull(message = "공개 여부는 필수입니다")
    @Column(name = "is_open", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isOpen = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 비즈니스 메서드
    public void updateBoard(String title, String description, String thumbnailUrl, Boolean isOpen) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (thumbnailUrl != null) this.thumbnailUrl = thumbnailUrl;
        if (isOpen != null) this.isOpen = isOpen;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decrementBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }

//    레시피 공유 삭제
    public void softDelete() { this.deletedAt = LocalDateTime.now();}

//   삭제 여부 확인
    public boolean isDeleted() { return this.deletedAt != null;}

    public boolean isPublic() {
        return this.isOpen != null && this.isOpen;
    }

    public void updatePost(String title, String description, String thumbnailUrl, CategoryEnum category, Boolean isOpen) {
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
        if (isOpen != null) {
            this.isOpen = isOpen;
        }
    }

    // 소유자 확인 (RecipeService 스타일과 통일)
    public boolean isOwnedBy(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }

    // Redis 동기화를 위한 조회수 직접 설정 메서드
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount != null ? viewCount : 0L;
    }

    // Redis 동기화를 위한 좋아요 수 직접 설정 메서드
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount != null ? likeCount : 0L;
    }

    // Redis 동기화를 위한 북마크 수 직접 설정 메서드
    public void setBookmarkCount(Long bookmarkCount) {
        this.bookmarkCount = bookmarkCount != null ? bookmarkCount : 0L;
    }
}
