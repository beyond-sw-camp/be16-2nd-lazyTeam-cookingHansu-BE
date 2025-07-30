package lazyteam.cooking_hansu.domain.comment.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.recipe.entity.RecipeStep;
import lombok.*;

/**
 * 레시피 공유 게시글의 조리순서별 추가 코멘트 엔티티
 * ERD의 post_comment 테이블과 매핑
 * 하나의 조리순서에 하나의 코멘트
 */
@Entity
@Table(name = "post_comment",
       uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "step_id"}))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class PostComment extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post postId; // 게시글 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private RecipeStep stepId; // 조리순서 ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 코멘트 내용

    // ========== 비즈니스 메서드 ==========

    /**
     * 코멘트 내용 수정
     */
    public void updateContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
    }

    /**
     * 코멘트 유효성 확인
     */
    public boolean isValid() {
        return this.content != null && !this.content.trim().isEmpty();
    }

    /**
     * 특정 게시글의 특정 조리순서에 대한 코멘트인지 확인
     */
    public boolean belongsTo(Long postId, Long stepId) {
        return this.postId.equals(postId) && this.stepId.equals(stepId);
    }
}
