package lazyteam.cooking_hansu.domain.interaction.controller;

import lazyteam.cooking_hansu.domain.interaction.dto.LectureLikeInfoDto;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    // ========== 게시글 좋아요 ==========

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<ResponseDto<String>> togglePostLike(@PathVariable UUID postId) {
        UUID userId = UUID.fromString(testUserIdStr);
        String result = interactionService.togglePostLike(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @GetMapping("/posts/{postId}/likes/status")
    public ResponseEntity<ResponseDto<Boolean>> getPostLikeStatus(@PathVariable UUID postId) {
        UUID userId = UUID.fromString(testUserIdStr);
        boolean isLiked = interactionService.isPostLiked(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isLiked));
    }

    // ========== 게시글 북마크 ==========

    @PostMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<ResponseDto<String>> togglePostBookmark(@PathVariable UUID postId) {
        UUID userId = UUID.fromString(testUserIdStr);
        String result = interactionService.toggleBookmark(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @GetMapping("/posts/{postId}/bookmarks/status")
    public ResponseEntity<ResponseDto<Boolean>> getPostBookmarkStatus(@PathVariable UUID postId) {
        UUID userId = UUID.fromString(testUserIdStr);
        boolean isBookmarked = interactionService.isBookmarked(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isBookmarked));
    }

    // ========== 강의 좋아요 ==========

    @PostMapping("/lectures/{lectureId}/likes")
    public ResponseEntity<ResponseDto<String>> toggleLectureLike(@PathVariable UUID lectureId) {
        UUID userId = UUID.fromString(testUserIdStr);
        String result = interactionService.toggleLectureLike(lectureId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @GetMapping("/lectures/{lectureId}/likes/status")
    public ResponseEntity<ResponseDto<Boolean>> getLectureLikeStatus(@PathVariable UUID lectureId) {
        UUID userId = UUID.fromString(testUserIdStr);
        boolean isLiked = interactionService.isLectureLiked(lectureId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isLiked));
    }

    @GetMapping("/lectures/{lectureId}/likes/info")
    public ResponseEntity<ResponseDto<LectureLikeInfoDto>> getLectureLikeInfo(@PathVariable UUID lectureId) {
        UUID userId = UUID.fromString(testUserIdStr);
        LectureLikeInfoDto info = interactionService.getLectureLikeInfo(lectureId, userId);
        return ResponseEntity.ok(ResponseDto.ok(info));
    }

    // ========== 조회수 ==========

    @PostMapping("/posts/{postId}/views")
    public ResponseEntity<ResponseDto<String>> incrementViewCount(@PathVariable UUID postId) {
        interactionService.incrementViewCount(postId);
        return ResponseEntity.ok(ResponseDto.ok("조회수가 증가했습니다."));
    }

    @PostMapping("/posts/{postId}/views/check")
    public ResponseEntity<ResponseDto<String>> incrementViewCountWithCheck(@PathVariable UUID postId) {
        UUID userId = UUID.fromString(testUserIdStr);
        boolean incremented = interactionService.incrementViewCountWithCheck(postId, userId);
        String message = incremented ? "조회수가 증가했습니다." : "이미 조회한 게시글입니다.";
        return ResponseEntity.ok(ResponseDto.ok(message));
    }
}
