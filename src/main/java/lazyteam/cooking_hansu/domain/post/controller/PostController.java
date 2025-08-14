package lazyteam.cooking_hansu.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.service.PostService;
import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.recipe.entity.PostSequenceDescription;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts/recipes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recipe Post")
public class PostController {

    private final PostService postService;
    private final S3Uploader s3Uploader;
    private final InteractionService interactionService;

    // ========== 공개 레시피 공유 게시글 API (전체 사용자용) ==========

    // 레시피 공유게시글 목록 조회 (공개)
    @GetMapping
    public ResponseEntity<?> getRecipePosts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponseDto> posts;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postService.searchRecipePosts(keyword.trim(), pageable);
            log.info("레시피 게시글 검색 완료. 키워드: {}, 결과 수: {}", keyword, posts.getTotalElements());
        } else {
            posts = postService.getRecipePosts(pageable);
            log.info("레시피 게시글 목록 조회 완료. 총 개수: {}", posts.getTotalElements());
        }
        
        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    // 레시피 공유게시글 상세 조회 (공개)
    @GetMapping("/{postId}")
    public ResponseEntity<?> getRecipePost(@PathVariable UUID postId) {
        PostResponseDto post = postService.getRecipePost(postId);
        
        return ResponseEntity.ok(ResponseDto.ok(post, HttpStatus.OK));
    }

    // 특정 사용자의 공개 게시글 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRecipePostsByUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponseDto> posts = postService.getRecipePostsByUser(userId, pageable);
        log.info("사용자 {} 레시피 공유게시글 목록 조회 완료. 총 개수: {}", userId, posts.getTotalElements());
        
        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    // 카테고리별 레시피 공유게시글 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getRecipePostsByCategory(
            @PathVariable CategoryEnum category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponseDto> posts = postService.getRecipePostsByCategory(category, pageable);
        log.info("카테고리 {} 레시피 공유게시글 목록 조회 완료. 총 개수: {}", category, posts.getTotalElements());
        
        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    // ========== 상호작용 기능 ==========

    // 좋아요 추가/취소
    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> toggleLike(@PathVariable UUID postId, @RequestParam UUID userId) {
        String result = interactionService.toggleLike(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result, HttpStatus.OK));
    }

    // 북마크 추가/취소
    @PostMapping("/{postId}/bookmarks")
    public ResponseEntity<?> toggleBookmark(@PathVariable UUID postId, @RequestParam UUID userId) {
        String result = interactionService.toggleBookmark(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result, HttpStatus.OK));
    }

    // 좋아요 상태 확인
    @GetMapping("/{postId}/likes/status")
    public ResponseEntity<?> getLikeStatus(@PathVariable UUID postId, @RequestParam UUID userId) {
        boolean isLiked = interactionService.isLiked(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isLiked, HttpStatus.OK));
    }

    // 북마크 상태 확인
    @GetMapping("/{postId}/bookmarks/status")
    public ResponseEntity<?> getBookmarkStatus(@PathVariable UUID postId, @RequestParam UUID userId) {
        boolean isBookmarked = interactionService.isBookmarked(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isBookmarked, HttpStatus.OK));
    }
    
    // ========== 레시피 연결 관련 API ==========
    
    @Operation(summary = "게시글의 레시피 연결 정보 조회", description = "게시글에 연결된 레시피와 단계별 설명을 조회합니다.")
    @GetMapping("/{postId}/recipe")
    public ResponseEntity<?> getConnectedRecipe(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId) {
        
        Recipe connectedRecipe = postService.getConnectedRecipe(postId);
        
        if (connectedRecipe == null) {
            return ResponseEntity.ok(ResponseDto.ok( "연결된 레시피가 없습니다.",HttpStatus.OK ));
        }
        
        return ResponseEntity.ok(ResponseDto.ok(connectedRecipe, HttpStatus.OK));
    }
    
    @Operation(summary = "게시글의 레시피 단계별 설명 조회", description = "게시글에 연결된 레시피의 단계별 설명을 조회합니다.")
    @GetMapping("/{postId}/recipe/steps")
    public ResponseEntity<?> getRecipeStepDescriptions(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId) {
        
        List<PostSequenceDescription> descriptions = postService.getPostRecipeDescriptions(postId);
        
        return ResponseEntity.ok(ResponseDto.ok(descriptions, HttpStatus.OK));
    }
}