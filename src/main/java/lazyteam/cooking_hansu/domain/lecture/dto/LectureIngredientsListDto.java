package lazyteam.cooking_hansu.domain.lecture.dto;


import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureIngredientsList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class LectureIngredientsListDto {
    private String ingredientsName;
    private String amount;

    public LectureIngredientsList toEntity(Lecture lecture) {
        return LectureIngredientsList.builder()
                .amount(amount)
                .lecture(lecture)
                .IngredientsName(ingredientsName)
                .build();
    }
}
