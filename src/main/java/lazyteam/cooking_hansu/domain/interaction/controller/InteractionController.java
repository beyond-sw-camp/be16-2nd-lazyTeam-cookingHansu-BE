package lazyteam.cooking_hansu.domain.interaction.controller;

import lazyteam.cooking_hansu.domain.interaction.dto.LectureLikeInfoDto;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    // ========== 게시글 관련 ==========

    @PostMapping("/likes/{postId}")
    public ResponseEntity<ResponseDto<String>> toggleLike(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        String result = interactionService.togglePostLike(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @GetMapping("/likes/{postId}")
    public ResponseEntity<ResponseDto<Boolean>> getLikeStatus(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        boolean isLiked = interactionService.isPostLiked(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isLiked));
    }

    @PostMapping("/bookmarks/{postId}")
    public ResponseEntity<ResponseDto<String>> toggleBookmark(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        String result = interactionService.toggleBookmark(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @GetMapping("/bookmarks/{postId}")
    public ResponseEntity<ResponseDto<Boolean>> getBookmarkStatus(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        boolean isBookmarked = interactionService.isBookmarked(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isBookmarked));
    }

    // ========== 강의 관련 ==========

    @PostMapping("/lecture-likes/{lectureId}")
    public ResponseEntity<ResponseDto<String>> toggleLectureLike(
            @PathVariable UUID lectureId,
            @RequestParam UUID userId) {
        
        String result = interactionService.toggleLectureLike(lectureId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @GetMapping("/lecture-likes/{lectureId}")
    public ResponseEntity<ResponseDto<Boolean>> getLectureLikeStatus(
            @PathVariable UUID lectureId,
            @RequestParam UUID userId) {
        
        boolean isLiked = interactionService.isLectureLiked(lectureId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isLiked));
    }

    @GetMapping("/lecture-likes/{lectureId}/info")
    public ResponseEntity<ResponseDto<LectureLikeInfoDto>> getLectureLikeInfo(
            @PathVariable UUID lectureId,
            @RequestParam(required = false) UUID userId) {
        
        LectureLikeInfoDto info = interactionService.getLectureLikeInfo(lectureId, userId);
        return ResponseEntity.ok(ResponseDto.ok(info));
    }

    // ========== 조회수 ==========

    @PostMapping("/views/{postId}")
    public ResponseEntity<ResponseDto<String>> incrementViewCount(@PathVariable UUID postId) {
        interactionService.incrementViewCount(postId);
        return ResponseEntity.ok(ResponseDto.ok("조회수가 증가했습니다."));
    }

    @PostMapping("/views/{postId}/check")
    public ResponseEntity<ResponseDto<String>> incrementViewCountWithCheck(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        boolean incremented = interactionService.incrementViewCountWithCheck(postId, userId);
        String message = incremented ? "조회수가 증가했습니다." : "이미 조회한 게시글입니다.";
        return ResponseEntity.ok(ResponseDto.ok(message));
    }
}
