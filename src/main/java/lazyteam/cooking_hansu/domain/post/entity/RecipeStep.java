package lazyteam.cooking_hansu.domain.post.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdEntity;
import lombok.*;

@Entity
@Table(name = "Recipe_Step")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class RecipeStep extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // ERD 통합: post_id 참조

    @Column(name = "step_sequence", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer stepSequence; // 순서 번호 (1, 2, 3, ...)

    @Column(nullable = false, length = 255)
    private String content; // 조리 단계 설명

    // ========== 비즈니스 메서드 ==========



    /**
     * 조리 단계 유효성 확인
     */
    public boolean isValid() {
        return this.content != null && !this.content.trim().isEmpty() &&
               this.stepSequence != null && this.stepSequence > 0;
    }
}
