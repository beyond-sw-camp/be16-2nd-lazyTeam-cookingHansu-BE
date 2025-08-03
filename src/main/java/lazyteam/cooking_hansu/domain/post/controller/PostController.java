package lazyteam.cooking_hansu.domain.post.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.service.PostService;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/posts/recipes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recipe Post")
public class PostController {

    private final PostService postService;

    // 레시피 공유게시글 목록 조회
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

    // 레시피 공유게시글 생성
    @PostMapping
    public ResponseEntity<?> createRecipePost(@Valid @RequestBody PostCreateRequestDto requestDto) {
        UUID postId = postService.createRecipePost(requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(postId, HttpStatus.CREATED));
    }

    // 레시피 공유게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<?> getRecipePost(@PathVariable UUID postId) {
        PostResponseDto post = postService.getRecipePost(postId);
        
        return ResponseEntity.ok(ResponseDto.ok(post, HttpStatus.OK));
    }

    // 레시피 공유게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<?> updateRecipePost(
            @PathVariable UUID postId,
            @Valid @RequestBody PostCreateRequestDto requestDto
    ) {
        postService.updateRecipePost(postId, requestDto);
        
        return ResponseEntity.ok(ResponseDto.ok(postId, HttpStatus.OK));
    }

    // 레시피 공유게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deleteRecipePost(@PathVariable UUID postId) {
        postService.deleteRecipePost(postId);
        
        return ResponseEntity.ok(ResponseDto.ok(postId, HttpStatus.OK));
    }

    // 내 레시피 공유게시글 목록 조회
    @GetMapping("/my")
    public ResponseEntity<?> getMyRecipePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponseDto> posts = postService.getMyRecipePosts(pageable);
        log.info("내 레시피 공유게시글 목록 조회 완료. 총 개수: {}", posts.getTotalElements());
        
        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
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
}
