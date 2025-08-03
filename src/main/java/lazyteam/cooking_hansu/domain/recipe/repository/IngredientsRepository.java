package lazyteam.cooking_hansu.domain.recipe.repository;

import lazyteam.cooking_hansu.domain.recipe.entity.Ingredients;
import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IngredientsRepository extends JpaRepository<Ingredients, UUID> {

//    레시피 조회시 재료 목록 필요
    List<Ingredients>findByRecipe(Recipe recipe);

//    레시피 수정/삭제시 재료관리
    void deleteByRecipe(Recipe recipe);
}
