package lazyteam.cooking_hansu.domain.comment.controller;

import lazyteam.cooking_hansu.domain.comment.dto.PostCommentCreateDto;
import lazyteam.cooking_hansu.domain.comment.service.PostCommentService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("post/comments")
public class PostCommentController {

    private final PostCommentService postCommentService;

    @PostMapping("/create")
    public ResponseEntity<?> createComment(@RequestBody PostCommentCreateDto postCommentCreateDto) {
        postCommentService.createComment(postCommentCreateDto);
        return new ResponseEntity<>(ResponseDto.ok("댓글이 등록되었습니다.", HttpStatus.CREATED), HttpStatus.CREATED);
    }
}
