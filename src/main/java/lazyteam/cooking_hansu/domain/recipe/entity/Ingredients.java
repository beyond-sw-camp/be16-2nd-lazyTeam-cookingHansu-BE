package lazyteam.cooking_hansu.domain.recipe.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lombok.*;

@Entity
@Table(name = "Ingredients_list")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Ingredients extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId; // 어떤 레시피의 재료인지

    @Column(nullable = false, length = 255)
    private String name; // 재료명 (예: 돼지고기, 김치, 양파)

    @Column(nullable = false, length = 255)
    private String amount; // 재료량 (예: 200g, 1/2개, 2컵)

//    비즈니스메서드

//    재료정보수정
    public void updateIngredient(String name, String amount) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (amount != null && !amount.trim().isEmpty()) {
            this.amount = amount.trim();
        }
    }

//    재료정보 유효성 수정
    public boolean isValid() {
        return this.name != null && !this.name.trim().isEmpty() &&
               this.amount != null && !this.amount.trim().isEmpty();
    }
}
