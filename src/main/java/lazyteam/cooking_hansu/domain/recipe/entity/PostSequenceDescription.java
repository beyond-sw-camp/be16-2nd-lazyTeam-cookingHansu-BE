package lazyteam.cooking_hansu.domain.recipe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdEntity;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lombok.*;

/**
 * 레시피 공유 게시글의 조리순서별 설명 엔티티
 * ERD의 post_sequence_description 테이블과 매핑
 */
@Entity
@Table(name = "post_sequence_description")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class PostSequenceDescription extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // 게시글과 조리순서 유니크(하나의 조리순서에 하나의 설명)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private RecipeStep recipeStep; // 레시피 단계

    @NotBlank(message = "조리순서 설명은 필수입니다")
    @Size(max = 2000, message = "조리순서 설명은 2000자 이하여야 합니다")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 해당 조리순서에 대한 추가 설명/코멘트

    // ========== 비즈니스 메서드 ==========

    /**
     * 설명 내용 수정
     */
    public void updateContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
    }

    /**
     * 게시글과 조리순서의 매핑이 유효한지 확인
     */
    public boolean isValidMapping() {
        return this.post != null && this.recipeStep != null && 
               this.content != null && !this.content.trim().isEmpty();
    }

    /**
     * 특정 게시글의 조리순서인지 확인
     */
    public boolean belongsToPost(Post post) {
        return this.post != null && this.post.getId().equals(post.getId());
    }

    /**
     * 특정 레시피 단계의 설명인지 확인
     */
    public boolean isDescriptionForStep(RecipeStep step) {
        return this.recipeStep != null && this.recipeStep.getId().equals(step.getId());
    }
}
