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

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<ResponseDto<String>> togglePostLike(@PathVariable UUID postId) {
        String result = interactionService.togglePostLike(postId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @PostMapping("/posts/{postId}/bookmarks")
    public ResponseEntity<ResponseDto<String>> togglePostBookmark(@PathVariable UUID postId) {
        String result = interactionService.toggleBookmark(postId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @PostMapping("/lectures/{lectureId}/likes")
    public ResponseEntity<ResponseDto<String>> toggleLectureLike(@PathVariable UUID lectureId) {
        String result = interactionService.toggleLectureLike(lectureId);
        return ResponseEntity.ok(ResponseDto.ok(result));
    }

    @PostMapping("/posts/{postId}/views")
    public ResponseEntity<ResponseDto<String>> incrementViewCount(@PathVariable UUID postId) {
        interactionService.incrementViewCount(postId);
        return ResponseEntity.ok(ResponseDto.ok("조회수가 증가했습니다."));
    }

    @PostMapping("/posts/{postId}/views/check")
    public ResponseEntity<ResponseDto<String>> incrementViewCountWithCheck(@PathVariable UUID postId) {
        boolean incremented = interactionService.incrementViewCountWithCheck(postId);
        String message = incremented ? "조회수가 증가했습니다." : "이미 조회한 게시글입니다.";
        return ResponseEntity.ok(ResponseDto.ok(message));
    }
}