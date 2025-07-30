package lazyteam.cooking_hansu.domain.recipe.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lombok.*;

/**
 * 조리 순서 엔티티
 * ERD의 Lecture_Step 테이블과 매핑
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class RecipeStep extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipeId; // 어떤 레시피의 조리 순서인지

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer stepSequence; // 순서 번호 (1, 2, 3, ...)

    @Column(nullable = false, length = 255)
    private String content; // 조리 단계 설명

    // ========== 비즈니스 메서드 ==========

    /**
     * 조리 순서 내용 수정
     */
    public void updateContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
    }

    /**
     * 순서 번호 수정
     */
    public void updateSequence(Integer stepSequence) {
        if (stepSequence != null && stepSequence > 0) {
            this.stepSequence = stepSequence;
        }
    }

    /**
     * 조리 단계 유효성 확인
     */
    public boolean isValid() {
        return this.content != null && !this.content.trim().isEmpty() &&
               this.stepSequence != null && this.stepSequence > 0;
    }

    /**
     * 다음 단계인지 확인
     */
    public boolean isNextStep(Integer currentSequence) {
        return this.stepSequence.equals(currentSequence + 1);
    }
}
