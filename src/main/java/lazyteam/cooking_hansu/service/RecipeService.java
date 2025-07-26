package lazyteam.cooking_hansu.service;

import jakarta.persistence.criteria.Predicate;
import lazyteam.cooking_hansu.domain.Recipe;
import lazyteam.cooking_hansu.dto.RecipeListDto;
import lazyteam.cooking_hansu.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class RecipeService {

        private final RecipeRepository recipeRepository;

        public Page<RecipeListDto> filteredRecipeList(Pageable pageable, String userType, String category, String sortBy) {
            // 정렬 설정
            Sort sort;
            if ("popular".equalsIgnoreCase(sortBy)) {
                // 인기순 (임시로 id 기준, likeCount 추가되면 교체예정)
                sort = Sort.by(Sort.Direction.DESC, "id");
            } else {
                // 최신순
                sort = Sort.by(Sort.Direction.DESC, "id");
            }

            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

            // 카테고리 필터
            Page<Recipe> recipes;
            if (category != null && !category.isEmpty()) {
                recipes = recipeRepository.findByCategory(
                        Recipe.Category.valueOf(category.toUpperCase()), sortedPageable
                );
            } else {
                recipes = recipeRepository.findAll(sortedPageable);
            }

            return recipes.map(RecipeListDto::fromEntity);
        }

    // 레시피 상세 조회
    public Recipe recipeDetail(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 레시피가 없습니다. id=" + id));
    }
}
