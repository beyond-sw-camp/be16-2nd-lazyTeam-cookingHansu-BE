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

    @Column(nullable = false)
    private String content; // 조리 단계 설명

    @Column(columnDefinition = "TEXT")
    private String description; // 추가 설명

}
