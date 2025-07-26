package lazyteam.cooking_hansu.controller;

import lazyteam.cooking_hansu.dto.RecipeListDto;
import lazyteam.cooking_hansu.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "http://localhost:5173")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<Page<RecipeListDto>> recipeList(
            @PageableDefault(size = 8) Pageable pageable,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String category,
//            @RequestParam(defaultValue = "latest") String sortBy 레시피 공유게시글이 있어야, 생성순으로 정렬할 수 있음
            // 우선 임시로 id 값으로 정렬
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(recipeService.filteredRecipeList(pageable, userType, category, sortBy));
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> recipeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.recipeDetail(id));
    }
}
