package lazyteam.cooking_hansu.domain.comment.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentCreateDto;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentListResDto;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentUpdateDto;
import lazyteam.cooking_hansu.domain.comment.service.PostCommentService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post/comment")
public class PostCommentController {

    private final PostCommentService postCommentService;

//    댓글 생성
    @PostMapping("/create")
    public ResponseEntity<?> createComment(@Valid @RequestBody PostCommentCreateDto postCommentCreateDto) {
        UUID commentId = postCommentService.createComment(postCommentCreateDto);
        return new ResponseEntity<>(ResponseDto.ok(commentId, HttpStatus.CREATED), HttpStatus.CREATED);
    }

//    댓글 목록 조회 (페이지네이션)
    @GetMapping("/list/{postId}")
    public ResponseEntity<?> getCommentList(@PathVariable UUID postId, Pageable pageable) {
        Page<PostCommentListResDto> commentPage = postCommentService.findCommentList(postId, pageable);
        return new ResponseEntity<>(ResponseDto.ok(commentPage, HttpStatus.OK), HttpStatus.OK);
    }

//    댓글 수정
    @PatchMapping("/update/{commentId}")
    @PreAuthorize("hasAnyRole('GENERAL', 'CHEF', 'OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateComment(@PathVariable UUID commentId, @Valid @RequestBody PostCommentUpdateDto postCommentUpdateDto) {
        UUID updateCommentId = postCommentService.updateComment(commentId, postCommentUpdateDto);
        return new ResponseEntity<>(ResponseDto.ok(updateCommentId, HttpStatus.OK), HttpStatus.OK);
    }

//    댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    @PreAuthorize("hasAnyRole('GENERAL', 'CHEF', 'OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteComment(@PathVariable UUID commentId) {
        postCommentService.deleteComment(commentId);
        return new ResponseEntity<>(ResponseDto.ok("댓글이 삭제되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }
}
