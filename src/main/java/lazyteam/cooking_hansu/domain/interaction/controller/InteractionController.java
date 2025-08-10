package lazyteam.cooking_hansu.domain.interaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lazyteam.cooking_hansu.domain.interaction.dto.InteractionCountDto;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@Tag(name = "상호작용 API", description = "좋아요, 북마크 관리")
public class InteractionController {

    private final InteractionService interactionService;

    @Operation(summary = "좋아요 추가/취소", description = "게시글 좋아요를 추가하거나 취소합니다")
    @PostMapping("/likes/{postId}")
    public ResponseEntity<ResponseDto<String>> toggleLike(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        String result = interactionService.toggleLike(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result, HttpStatus.OK));
    }

    @Operation(summary = "북마크 추가/취소", description = "게시글 북마크를 추가하거나 취소합니다")
    @PostMapping("/bookmarks/{postId}")
    public ResponseEntity<ResponseDto<String>> toggleBookmark(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        String result = interactionService.toggleBookmark(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(result, HttpStatus.OK));
    }

    @Operation(summary = "좋아요 상태 확인", description = "사용자가 해당 게시글을 좋아요했는지 확인합니다")
    @GetMapping("/likes/{postId}/status")
    public ResponseEntity<ResponseDto<Boolean>> getLikeStatus(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        boolean isLiked = interactionService.isLiked(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isLiked, HttpStatus.OK));
    }

    @Operation(summary = "북마크 상태 확인", description = "사용자가 해당 게시글을 북마크했는지 확인합니다")
    @GetMapping("/bookmarks/{postId}/status")
    public ResponseEntity<ResponseDto<Boolean>> getBookmarkStatus(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        
        boolean isBookmarked = interactionService.isBookmarked(postId, userId);
        return ResponseEntity.ok(ResponseDto.ok(isBookmarked, HttpStatus.OK));
    }

    @Operation(summary = "게시글 상호작용 카운트 조회", description = "게시글의 좋아요, 북마크 개수를 조회합니다")
    @GetMapping("/counts/{postId}")
    public ResponseEntity<ResponseDto<InteractionCountDto>> getInteractionCounts(
            @PathVariable UUID postId) {
        
        InteractionCountDto counts = interactionService.getInteractionCounts(postId);
        return ResponseEntity.ok(ResponseDto.ok(counts, HttpStatus.OK));
    }
}
