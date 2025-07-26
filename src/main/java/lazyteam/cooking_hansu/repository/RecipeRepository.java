package lazyteam.cooking_hansu.repository;

import lazyteam.cooking_hansu.domain.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    // 카테고리로 필터
    Page<Recipe> findByCategory(Recipe.Category category, Pageable pageable);

    // (추가) 사용자 유형까지 필터해야 한다면
    // Recipe에는 사용자 유형은 없으므로 추후에 user 테이블과 join 필요
    Page<Recipe> findByUserIdAndCategory(Long userId, Recipe.Category category, Pageable pageable);
}

