package lazyteam.cooking_hansu.domain.recipe.controller;

import lazyteam.cooking_hansu.domain.recipe.dto.*;
import lazyteam.cooking_hansu.domain.recipe.service.RecipeService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lazyteam.cooking_hansu.global.service.S3Uploader;

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
@RequestMapping("/api/recipes")  // 경로 변경: 공개 레시피 관련만
public class RecipeController {

    private final RecipeService recipeService;
    private final S3Uploader s3Uploader;

    // 공개 레시피 목록 조회 (전체 사용자용)
    @GetMapping
    public ResponseEntity<?> getPublicRecipes(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // TODO: 공개 레시피 조회 로직 구현 필요 (추후 구현)
        log.info("공개 레시피 조회 API - 추후 구현 예정");
        
        return ResponseEntity.ok(ResponseDto.ok("공개 레시피 조회 API - 추후 구현 예정", HttpStatus.OK));
    }

    // 레시피 상세 조회 (공개용)
    @GetMapping("/{recipeId}")
    public ResponseEntity<?> getPublicRecipe(@PathVariable UUID recipeId) {
        // TODO: 공개 레시피 상세 조회 로직 구현 필요 (추후 구현)
        log.info("공개 레시피 상세 조회 API - 추후 구현 예정. recipeId: {}", recipeId);
        
        return ResponseEntity.ok(ResponseDto.ok("공개 레시피 상세 조회 API - 추후 구현 예정", HttpStatus.OK));
    }
}
