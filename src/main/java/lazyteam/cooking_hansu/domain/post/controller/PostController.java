package lazyteam.cooking_hansu.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostUpdateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.service.PostService;
import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.recipe.entity.PostSequenceDescription;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
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

    @Operation(summary = "레시피 공유 게시글 생성", description = "새로운 레시피 공유 게시글을 생성합니다.")
    @PostMapping
    public ResponseEntity<?> createRecipePost(
            @Valid @RequestPart("request") PostCreateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        UUID postId = postService.createRecipePost(requestDto, thumbnail);
        return ResponseEntity.ok(ResponseDto.ok(postId, HttpStatus.CREATED));
    }

    @Operation(summary = "레시피 공유 게시글 상세 조회", description = "게시글과 연결된 레시피, 재료, 조리 과정, 추가 코멘트를 반환합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<?> getRecipePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId) {
        boolean incremented = interactionService.incrementViewCountWithCheck(postId);
        if (incremented) {
            log.info("조회수 증가됨: postId={}", postId);
        } else {
            log.debug("중복 조회 - 조회수 증가하지 않음: postId={}", postId);
        }

        PostResponseDto post = postService.getRecipePost(postId);
        return ResponseEntity.ok(ResponseDto.ok(post, HttpStatus.OK));
    }

    @Operation(summary = "레시피 공유 게시글 목록 조회", description = "공개된 게시글을 필터링하여 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getRecipePosts(
            @RequestParam(required = false) CategoryEnum category,
            @RequestParam(required = false) Role userType,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> posts;

        if (category != null && userType != null) {
            posts = postService.getRecipePostsByCategoryAndUserRole(category, userType, pageable);
            log.info("카테고리+유저타입 필터링 완료. 카테고리: {}, 타입: {}, 결과 수: {}", category, userType, posts.getTotalElements());
        } else if (category != null) {
            posts = postService.getRecipePostsByCategory(category, pageable);
            log.info("카테고리별 레시피 게시글 조회 완료. 카테고리: {}, 결과 수: {}", category, posts.getTotalElements());
        } else if (userType != null) {
            posts = postService.getRecipePostsByUserRole(userType, pageable);
            log.info("유저타입별 레시피 게시글 조회 완료. 타입: {}, 결과 수: {}", userType, posts.getTotalElements());
        } else {
            posts = postService.getRecipePostsByUserRole(null, pageable);
            log.info("전체 공개 게시글 조회 완료. 결과 수: {}", posts.getTotalElements());
        }

        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    @Operation(summary = "내 레시피 공유 게시글 조회", description = "현재 사용자의 게시글을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<?> getMyRecipePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> posts = postService.getMyRecipePosts(pageable);
        log.info("내 레시피 공유게시글 목록 조회 완료. 총 개수: {}", posts.getTotalElements());
        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    @Operation(summary = "특정 사용자의 공개 게시글 조회", description = "특정 사용자의 공개된 게시글을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRecipePostsByUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> posts = postService.getRecipePostsByUser(userId, pageable);
        log.info("사용자 {} 레시피 공유게시글 목록 조회 완료. 총 개수: {}", userId, posts.getTotalElements());
        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    @Operation(summary = "카테고리별 레시피 공유 게시글 조회", description = "특정 카테고리의 공개된 게시글을 조회합니다.")
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getRecipePostsByCategory(
            @PathVariable CategoryEnum category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> posts = postService.getRecipePostsByCategory(category, pageable);
        log.info("카테고리 {} 레시피 공유게시글 목록 조회 완료. 총 개수: {}", category, posts.getTotalElements());
        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }

    @Operation(summary = "게시글의 레시피 연결 정보 조회", description = "게시글에 연결된 레시피와 단계별 설명을 조회합니다.")
    @GetMapping("/{postId}/recipe")
    public ResponseEntity<?> getConnectedRecipe(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId) {
        Recipe connectedRecipe = postService.getConnectedRecipe(postId);
        if (connectedRecipe == null) {
            return ResponseEntity.ok(ResponseDto.ok("연결된 레시피가 없습니다.", HttpStatus.OK));
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

    @Operation(summary = "레시피 공유 게시글 수정")
    @PutMapping("/{postId}")
    public ResponseEntity<?> updateRecipePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId,
            @Valid @RequestPart("request") PostUpdateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        postService.updateRecipePost(postId, requestDto, thumbnail);
        return ResponseEntity.ok(ResponseDto.ok("게시글 수정 완료", HttpStatus.OK));
    }

    @Operation(summary = "레시피 공유 게시글 삭제")
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deleteRecipePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId) {
        postService.deleteRecipePost(postId);
        return ResponseEntity.ok(ResponseDto.ok("게시글 삭제 완료", HttpStatus.OK));
    }
}