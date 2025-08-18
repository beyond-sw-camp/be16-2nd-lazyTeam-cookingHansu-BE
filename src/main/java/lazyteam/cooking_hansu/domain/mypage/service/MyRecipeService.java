package lazyteam.cooking_hansu.domain.mypage.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.mypage.dto.MyRecipeListDto;
import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.recipe.repository.RecipeRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MyRecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    @Transactional(readOnly = true)
    public List<MyRecipeListDto> myRecipeList() {
        UUID testUserId = UUID.fromString( testUserIdStr);
        User user = userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));


        List<Recipe> recipes = recipeRepository.findAllByUser(user);

        return recipes.stream()
                .map(recipe -> MyRecipeListDto.builder()
                        .id(recipe.getId())
                        .title(recipe.getTitle())
                        .ingredients(
                                recipe.getIngredients().stream()
                                        .map(i -> i.getName() + " " + i.getAmount())
                                        .collect(Collectors.toList())
                        )
                        .createdAt(recipe.getCreatedAt())
                        .thumbnailUrl(recipe.getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());

    }
}
