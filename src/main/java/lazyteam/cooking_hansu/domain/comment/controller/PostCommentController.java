package lazyteam.cooking_hansu.domain.comment.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentCreateDto;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentListResDto;
import lazyteam.cooking_hansu.domain.comment.service.PostCommentService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post/comment")
public class PostCommentController {

    private final PostCommentService postCommentService;

//    댓글 생성
    @PostMapping("/create")
    public ResponseEntity<?> createComment(@Valid @RequestBody PostCommentCreateDto postCommentCreateDto) {
        postCommentService.createComment(postCommentCreateDto);
        return new ResponseEntity<>(ResponseDto.ok("댓글이 등록되었습니다.", HttpStatus.CREATED), HttpStatus.CREATED);
    }

//    댓글 목록 조회
    @GetMapping("/list/{postId}")
    public ResponseEntity<?> getCommentList(@PathVariable UUID postId) {
        List<PostCommentListResDto> commentList = postCommentService.findCommentList(postId);
        return new ResponseEntity<>(ResponseDto.ok(commentList, HttpStatus.OK), HttpStatus.OK);
    }
}
