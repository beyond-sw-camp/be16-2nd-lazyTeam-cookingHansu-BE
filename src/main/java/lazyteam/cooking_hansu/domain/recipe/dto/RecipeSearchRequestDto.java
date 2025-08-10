package lazyteam.cooking_hansu.domain.recipe.dto;

import lombok.*;

/**
 * 레시피 검색 요청 DTO
 * 내 레시피 페이지에서 간단한 키워드 검색용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeSearchRequestDto {

    private String keyword; // 제목 검색용 키워드
}
