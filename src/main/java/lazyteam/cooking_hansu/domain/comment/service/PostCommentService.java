package lazyteam.cooking_hansu.domain.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentCreateDto;
import lazyteam.cooking_hansu.domain.comment.entity.PostComment;
import lazyteam.cooking_hansu.domain.comment.repository.PostCommentRepository;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public void createComment(PostCommentCreateDto postCommentCreateDto) {

        // 유저가 존재하는지 확인
        UUID userId =UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저가 존재하지 않습니다."));
        Post post = postRepository.findById(postCommentCreateDto.getPostId()).orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        PostComment postComment;
        // 대댓글인 경우 부모 댓글을 찾아서 설정
        // 부모 댓글이 없는 경우 null로 설정
        if(postCommentCreateDto.getParentCommentId() != null) {
            PostComment parentComment = postCommentRepository.findById(postCommentCreateDto.getParentCommentId()).orElseThrow(() -> new EntityNotFoundException("부모 댓글이 존재하지 않습니다."));
            //  depth 1 제한
            if (parentComment.getParentComment() != null) {
                throw new IllegalArgumentException("대댓글에는 대댓글을 작성할 수 없습니다.");
            }
            postComment = PostComment.builder()
                    .post(post)
                    .parentComment(parentComment)
                    .user(user)
                    .content(postCommentCreateDto.getContent())
                    .build();
        }
        else {
            postComment = PostComment.builder()
                    .post(post)
                    .parentComment(null)
                    .user(user)
                    .content(postCommentCreateDto.getContent())
                    .build();
        }
        postCommentRepository.save(postComment);
    }
}
