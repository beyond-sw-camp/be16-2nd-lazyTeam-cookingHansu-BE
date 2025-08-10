package lazyteam.cooking_hansu.domain.recipe.repository;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID>{

    List<Recipe> findAllByUser(User user);

    // 사용자별 개인 레시피 조회
    List<Recipe> findByUser(User user);
    Page<Recipe> findByUser(User user, Pageable pageable);

    // 개인 레시피 내 검색
    Page<Recipe> findByUserAndTitleContaining(User user, String keyword, Pageable pageable);

}
