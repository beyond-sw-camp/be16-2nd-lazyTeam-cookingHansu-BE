package lazyteam.cooking_hansu.domain.recipe.controller;

import lazyteam.cooking_hansu.domain.recipe.dto.*;
import lazyteam.cooking_hansu.domain.recipe.service.RecipeService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    // 레시피 작성
    @PostMapping("/create")
    public ResponseEntity<?> createRecipe(
            @RequestPart("recipeData") @Valid RecipeCreateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        UUID recipeId = recipeService.createRecipe(requestDto, thumbnail);
        
        return new ResponseEntity<>(
                ResponseDto.ok(recipeId, HttpStatus.CREATED),
                HttpStatus.CREATED
        );
    }

    // 레시피 상세조회
    @GetMapping("/{recipeId}")
    public ResponseEntity<?> getRecipe(@PathVariable UUID recipeId) {
        RecipeResponseDto recipe = recipeService.getRecipe(recipeId);
        
        return ResponseEntity.ok(ResponseDto.ok(recipe, HttpStatus.OK));
    }

    // 레시피 수정
    @PutMapping("/{recipeId}")
    public ResponseEntity<?> updateRecipe(
            @PathVariable UUID recipeId,
            @RequestPart("recipeData") @Valid RecipeUpdateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        recipeService.updateRecipe(recipeId, requestDto, thumbnail);
        
        return ResponseEntity.ok(ResponseDto.ok(recipeId, HttpStatus.OK));
    }

    // 레시피 삭제
    @DeleteMapping("/{recipeId}")
    public ResponseEntity<?> deleteRecipe(@PathVariable UUID recipeId) {
        recipeService.deleteRecipe(recipeId);
        
        return ResponseEntity.ok(ResponseDto.ok(recipeId, HttpStatus.OK));
    }
}
