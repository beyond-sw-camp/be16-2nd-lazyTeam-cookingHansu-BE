package lazyteam.cooking_hansu.domain.post.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.service.PostService;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
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

@RestController
@RequestMapping("/api/posts/recipes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recipe Post")
public class PostController {

    private final PostService postService;
    private final InteractionService interactionService;

    // 레시피 공유게시글 목록 조회 (공개) - 필터링만 지원
    @GetMapping
    public ResponseEntity<?> getRecipePosts(
            @RequestParam(required = false) CategoryEnum category,
            @RequestParam(required = false) Role userType,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponseDto> posts;

        if (category != null && userType != null) {
            // 카테고리 + 유저타입 필터링
            posts = postService.getRecipePostsByCategoryAndUserRole(category, userType, pageable);
            log.info("카테고리+유저타입 필터링 완료. 카테고리: {}, 타입: {}, 결과 수: {}", category, userType, posts.getTotalElements());
        } else if (category != null) {
            // 카테고리 필터링
            posts = postService.getRecipePostsByCategory(category, pageable);
            log.info("카테고리별 레시피 게시글 조회 완료. 카테고리: {}, 결과 수: {}", category, posts.getTotalElements());
        } else if (userType != null) {
            // 유저타입 필터링
            posts = postService.getRecipePostsByUserRole(userType, pageable);
            log.info("유저타입별 레시피 게시글 조회 완료. 타입: {}, 결과 수: {}", userType, posts.getTotalElements());
        } else {
            // 필터 없이 전체 조회
            posts = postService.getRecipePosts(pageable);
            log.info("레시피 게시글 목록 조회 완료. 총 개수: {}", posts.getTotalElements());
        }

        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    // 레시피 공유게시글 생성
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createRecipePost(
            @RequestPart("data") @Valid PostCreateRequestDto requestDto,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        UUID postId = postService.createRecipePost(requestDto, thumbnailImage);

        return new ResponseEntity<>(
                ResponseDto.ok(postId, HttpStatus.CREATED),
                HttpStatus.CREATED
        );
    }

    // 레시피 공유게시글 상세 조회 (로그인 사용자는 조회수 증가)
    @GetMapping("/{postId}")
    public ResponseEntity<?> getRecipePost(@PathVariable UUID postId) {
        // 조회수 증가 (현재 로그인 사용자 기준으로 중복 체크 후 증가)
        boolean incremented = interactionService.incrementViewCountWithCheck(postId);
        if (incremented) {
            log.info("조회수 증가됨: postId={}", postId);
        } else {
            log.debug("중복 조회 - 조회수 증가하지 않음: postId={}", postId);
        }

        PostResponseDto post = postService.getRecipePost(postId);
        return ResponseEntity.ok(ResponseDto.ok(post, HttpStatus.OK));
    }

    // 내 레시피 공유게시글 수정을 위한 상세 조회 (비공개 게시글도 포함, 조회수 증가 안함)
    @GetMapping("/{postId}/edit")
    public ResponseEntity<?> getMyRecipePostForEdit(@PathVariable UUID postId) {
        PostResponseDto post = postService.getMyRecipePost(postId);
        return ResponseEntity.ok(ResponseDto.ok(post, HttpStatus.OK));
    }

    // 레시피 공유게시글 수정
    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateRecipePost(
            @PathVariable UUID postId,
            @RequestPart("data") @Valid PostCreateRequestDto requestDto,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        postService.updateRecipePost(postId, requestDto, thumbnailImage);

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