package lazyteam.cooking_hansu.domain.recipe.repository;

import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    List<Recipe> findAllByUser(User user);
}
