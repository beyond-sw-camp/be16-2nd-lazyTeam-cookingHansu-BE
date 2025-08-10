package lazyteam.cooking_hansu.domain.recipe.repository;

import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.recipe.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeStepRepository extends JpaRepository<RecipeStep, UUID> {

    // 레시피 조회시 조리순서 목록
    List<RecipeStep> findByRecipeOrderByStepSequence(Recipe recipe);

    //  레시피 수정/삭제 시 조리순서 관리
    void deleteByRecipe(Recipe recipe);
}
