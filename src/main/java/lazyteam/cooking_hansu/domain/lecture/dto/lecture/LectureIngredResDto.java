package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.lecture.entity.LectureIngredientsList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LectureIngredResDto {
    private String IngredientsName;
    private String amount;

    public static LectureIngredResDto fromEntity(LectureIngredientsList lectureIngredientsList) {
        return LectureIngredResDto.builder()
                .IngredientsName(lectureIngredientsList.getIngredientsName())
                .amount(lectureIngredientsList.getAmount())
                .build();
    }
}
