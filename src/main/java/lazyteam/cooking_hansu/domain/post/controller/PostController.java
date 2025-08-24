package lazyteam.cooking_hansu.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostUpdateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.dto.PostListResponseDto;
import lazyteam.cooking_hansu.domain.post.service.PostService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Post Management", description = "통합 Post 관리 API (레시피 + 게시글)")
public class PostController {

    private final PostService postService;
    private final InteractionService interactionService;

    @Operation(summary = "통합 Post 생성", description = "레시피 정보가 포함된 Post를 생성합니다. (제목, 설명, 재료, 조리순서, 썸네일 등)")
    @PostMapping
    public ResponseEntity<?> createPost(
            @Valid @RequestPart("request") PostCreateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {

        log.info("Post 생성 요청 - 제목: {}, 카테고리: {}, 난이도: {}, 조리시간: {}분, 인분: {}인분",
                requestDto.getTitle(), requestDto.getCategory(), requestDto.getLevel(),
                requestDto.getCookTime(), requestDto.getServing());

        UUID postId = postService.createPost(requestDto, thumbnail);

        return ResponseEntity.ok(ResponseDto.ok(postId, HttpStatus.CREATED));
    }

    @Operation(summary = "Post 상세 조회", description = "Post 정보와 함께 재료, 조리순서를 포함하여 반환합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(
            @Parameter(description = "Post ID", required = true)
            @PathVariable UUID postId) {

        // 조회수 증가 (Redis 기반 중복 방지)
        try {
            boolean incremented = interactionService.incrementViewCountWithCheck(postId);
            if (incremented) {
                log.info("조회수 증가됨: postId={}", postId);
            } else {
                log.debug("중복 조회 - 조회수 증가하지 않음: postId={}", postId);
            }
        } catch (Exception e) {
            log.warn("조회수 증가 실패, 조회는 계속 진행: postId={}, error={}", postId, e.getMessage());
        }

        PostResponseDto post = postService.getPost(postId);
        return ResponseEntity.ok(ResponseDto.ok(post, HttpStatus.OK));
    }

    @Operation(summary = "Post 수정", description = "Post의 기본 정보, 재료, 조리순서를 수정합니다.")
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestPart("request") PostUpdateRequestDto requestDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {

        postService.updatePost(postId, requestDto, thumbnail);

        return ResponseEntity.ok(ResponseDto.ok("Post 수정 완료", HttpStatus.OK));
    }

    @Operation(summary = "Post 삭제", description = "Post를 소프트 삭제하고 연관된 재료, 조리순서도 함께 삭제됩니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @Parameter(description = "Post ID", required = true)
            @PathVariable UUID postId) {

        postService.deletePost(postId);

        return ResponseEntity.ok(ResponseDto.ok("Post 삭제 완료", HttpStatus.OK));
    }

//    목록조회(필터추가)
    @Operation(summary = "Post 목록 조회", description = "필터링과 정렬 옵션을 포함한 Post 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getPostList(
            @Parameter(description = "사용자 유형 필터 (CHEF, OWNER, GENERAL)")
            @RequestParam(required = false) String userType,
            @Parameter(description = "카테고리 필터 (KOREAN, CHINESE, WESTERN, JAPANESE)")
            @RequestParam(required = false) CategoryEnum category,
            @Parameter(description = "정렬 방식 (latest, popular, likes, bookmarks)")
            @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "8") int size) {


        List<PostListResponseDto> posts = postService.getPostList(userType, category, sort, page, size);

        return ResponseEntity.ok(ResponseDto.ok(posts, HttpStatus.OK));
    }
}
