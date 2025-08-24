package lazyteam.cooking_hansu.domain.interaction.controller;

import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 상호작용 관련 API (좋아요, 북마크, 조회수)
 * - Redis 기반 고성능 처리
 */
@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionController {
    
    private final InteractionService interactionService;

    /**
     * 게시글 좋아요 토글
     */
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<ResponseDto<String>> togglePostLike(@PathVariable UUID postId) {
        String result = interactionService.togglePostLike(postId);
        return new ResponseEntity<>(ResponseDto.ok(result, HttpStatus.OK), HttpStatus.OK);
    }

    /**
     * 게시글 북마크 토글
     */
    @PostMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<ResponseDto<String>> togglePostBookmark(@PathVariable UUID postId) {
        String result = interactionService.toggleBookmark(postId);
        return new ResponseEntity<>(ResponseDto.ok(result, HttpStatus.OK), HttpStatus.OK);
    }

    /**
     * 강의 좋아요 토글
     */
    @PostMapping("/lectures/{lectureId}/likes")
    public ResponseEntity<ResponseDto<String>> toggleLectureLike(@PathVariable UUID lectureId) {
        String result = interactionService.toggleLectureLike(lectureId);
        return new ResponseEntity<>(ResponseDto.ok(result, HttpStatus.OK), HttpStatus.OK);
    }

    /**
     * 게시글 조회수 증가 (중복 방지 기능 포함)
     */
    @PostMapping("/posts/{postId}/views")
    public ResponseEntity<ResponseDto<String>> incrementViewCount(@PathVariable UUID postId) {
        boolean incremented = interactionService.incrementViewCountWithCheck(postId);
        String message = incremented ? "조회수가 증가했습니다." : "이미 조회한 게시글입니다.";
        return new ResponseEntity<>(ResponseDto.ok(message, HttpStatus.OK), HttpStatus.OK);
    }
}
