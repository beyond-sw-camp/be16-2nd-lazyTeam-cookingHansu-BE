package lazyteam.cooking_hansu.domain.post.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdEntity;
import lombok.*;

@Entity
@Table(name = "Ingredients_list")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Ingredients extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Post_id", nullable = false)
    private Post post; // 어떤 레시피의 재료인지

    @Column(nullable = false)
    private String name; // 재료명 (예: 돼지고기, 김치, 양파)

    @Column(nullable = false)
    private String amount; // 재료량 (예: 200g, 1/2개, 2컵)


}
