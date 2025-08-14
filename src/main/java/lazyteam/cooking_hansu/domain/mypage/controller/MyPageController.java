package lazyteam.cooking_hansu.domain.mypage.controller;

import lazyteam.cooking_hansu.domain.mypage.dto.*;
import lazyteam.cooking_hansu.domain.mypage.service.*;
import lazyteam.cooking_hansu.domain.recipe.dto.*;
import lazyteam.cooking_hansu.domain.recipe.service.RecipeService;
import lazyteam.cooking_hansu.domain.post.dto.*;
import lazyteam.cooking_hansu.domain.post.service.PostService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
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
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
@Slf4j
public class MyPageController {

    private final MyRecipeService myRecipeService;
    private final MyPostService myPostService;
    private final MyLectureService myLectureService;
    private final MyBookmarkService myBookmarkService;
    private final MyLikeService myLikeService;
    private final RecipeService recipeService;
    private final PostService postService;

    @GetMapping("/recipes")
    public ResponseEntity<?> myRecipeList() {
        return new ResponseEntity<>(
                ResponseDto.ok(myRecipeService.myRecipeList(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/posts")
    public ResponseEntity<?> myPostList() {
        return new ResponseEntity<>(
                ResponseDto.ok(myPostService.myPostList(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/lectures")
    public ResponseEntity<?> myLectures(@PageableDefault(size = 8) Pageable pageable) {
        return new ResponseEntity<>(
                ResponseDto.ok(myLectureService.myLectures(pageable), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/bookmarked-posts")
    public ResponseEntity<?> myBookmarkedPosts() {
        return new ResponseEntity<>(
                ResponseDto.ok(myBookmarkService.myBookmarkedPosts(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/liked-posts")
    public ResponseEntity<?> myLikedPosts() {
        return new ResponseEntity<>(
                ResponseDto.ok(myLikeService.myLikedPosts(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    // 레시피 작성
    @PostMapping("/recipes/create")
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

    // 내 레시피 목록조회
    @GetMapping("/recipes/list")
    public ResponseEntity<?> getMyRecipes(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RecipeResponseDto> recipes;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            recipes = recipeService.searchMyRecipes(keyword.trim(), pageable);
            log.info("내 레시피 검색 완료. 키워드: {}, 결과 수: {}", keyword, recipes.getTotalElements());
        } else {
            recipes = recipeService.getMyRecipes(pageable);
            log.info("내 레시피 목록 조회 완료. 총 개수: {}", recipes.getTotalElements());
        }
        
        return ResponseEntity.ok(ResponseDto.ok(recipes, HttpStatus.OK));
    }

    // 레시피 상세조회
    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<?> getRecipe(@PathVariable UUID recipeId) {
        RecipeResponseDto recipe = recipeService.getRecipe(recipeId);
        
        return ResponseEntity.ok(ResponseDto.ok(recipe, HttpStatus.OK));
    }

    // 레시피 수정
    @PutMapping("/recipes/{recipeId}")
    public ResponseEntity<?> updateRecipe(
            @PathVariable UUID recipeId,
            @RequestPart("recipeData") @Valid RecipeUpdateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        recipeService.updateRecipe(recipeId, requestDto, thumbnail);
        
        return ResponseEntity.ok(ResponseDto.ok(recipeId, HttpStatus.OK));
    }

    // 레시피 삭제
    @DeleteMapping("/recipes/{recipeId}")
    public ResponseEntity<?> deleteRecipe(@PathVariable UUID recipeId) {
        recipeService.deleteRecipe(recipeId);
        
        return ResponseEntity.ok(ResponseDto.ok(recipeId, HttpStatus.OK));
    }

    // 레시피 공유게시글 생성
    @PostMapping("/recipe-posts/create")
    public ResponseEntity<?> createRecipePost(
            @RequestPart("postData") @Valid PostCreateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        UUID postId = postService.createRecipePost(requestDto, thumbnail);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(postId, HttpStatus.CREATED));
    }

    // 내 레시피 공유게시글 목록 조회
    @GetMapping("/recipe-posts/list")
    public ResponseEntity<?> getMyRecipePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponseDto> posts = postService.getMyRecipePosts(pageable);
        log.info("내 레시피 공유게시글 목록 조회 완료. 총 개수: {}", posts.getTotalElements());
        
        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    // 레시피 공유게시글 상세 조회
    @GetMapping("/recipe-posts/{postId}")
    public ResponseEntity<?> getRecipePost(@PathVariable UUID postId) {
        PostResponseDto post = postService.getRecipePost(postId);
        
        return ResponseEntity.ok(ResponseDto.ok(post, HttpStatus.OK));
    }

    // 레시피 공유게시글 수정
    @PutMapping("/recipe-posts/{postId}")
    public ResponseEntity<?> updateRecipePost(
            @PathVariable UUID postId,
            @RequestPart("postData") @Valid PostCreateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        postService.updateRecipePost(postId, requestDto, thumbnail);
        
        return ResponseEntity.ok(ResponseDto.ok(postId, HttpStatus.OK));
    }

    // 레시피 공유게시글 삭제
    @DeleteMapping("/recipe-posts/{postId}")
    public ResponseEntity<?> deleteRecipePost(@PathVariable UUID postId) {
        postService.deleteRecipePost(postId);
        
        return ResponseEntity.ok(ResponseDto.ok(postId, HttpStatus.OK));
    }

    // 게시글의 레시피 연결 해제
    @DeleteMapping("/recipe-posts/{postId}/recipe")
    public ResponseEntity<?> unlinkRecipe(@PathVariable UUID postId) {
        postService.unlinkRecipeFromPost(postId);

        return ResponseEntity.ok(ResponseDto.ok("레시피 연결이 해제되었습니다.", HttpStatus.OK));
    }
}
