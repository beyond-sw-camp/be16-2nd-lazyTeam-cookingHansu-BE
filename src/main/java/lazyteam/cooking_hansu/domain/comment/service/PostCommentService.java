package lazyteam.cooking_hansu.domain.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentChildListResDto;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentCreateDto;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentListResDto;
import lazyteam.cooking_hansu.domain.comment.dto.PostCommentUpdateDto;
import lazyteam.cooking_hansu.domain.comment.entity.PostComment;
import lazyteam.cooking_hansu.domain.comment.repository.PostCommentRepository;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.auth.dto.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

//    댓글 생성
    public UUID createComment(PostCommentCreateDto postCommentCreateDto) {

        // 유저가 존재하는지 확인
        User user = getCurrentUser();
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
                    .user(user)
                    .content(postCommentCreateDto.getContent())
                    .build();
        }
        postCommentRepository.save(postComment);
        return postComment.getId();
    }

//    댓글 목록 조회
    @Transactional(readOnly = true)
    public List<PostCommentListResDto> findCommentList(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        List<PostComment> comments = postCommentRepository.findAllByPostAndParentCommentIsNull(post);
        return comments.stream()
                .filter(c -> !c.getCommentIsDeleted() || !c.getChildComments().isEmpty()) //  삭제된 댓글이지만 자식 있으면 남김
                .map(c -> PostCommentListResDto.builder()
                        .commentId(c.getId())
                        .postId(c.getPost().getId())
                        .authorId(c.getUser().getId())
                        .authorNickName(c.getUser().getNickname())
                        .authorProfileImage(c.getUser().getPicture())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .updatedAt(c.getUpdatedAt())
                        .isDeleted(c.getCommentIsDeleted())
                        .childComments(c.getChildComments().stream().map(child -> PostCommentChildListResDto.builder()
                                .commentId(child.getId())
                                .postId(child.getPost().getId())
                                .authorId(child.getUser().getId())
                                .authorNickName(child.getUser().getNickname())
                                .authorProfileImage(child.getUser().getPicture())
                                .content(child.getContent())
                                .createdAt(child.getCreatedAt())
                                .updatedAt(child.getUpdatedAt())
                                .build()).toList())
                        .build())
                .toList();
    }

    // 댓글 수정
    public UUID updateComment(UUID commentId, PostCommentUpdateDto postCommentUpdateDto) {
        PostComment postComment = postCommentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));
        postComment.updateContent(postCommentUpdateDto.getContent());
        return postComment.getId();
    }


    // 댓글 삭제
    public void deleteComment(UUID commentId) {
        PostComment postComment = postCommentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));

        if (postComment.getParentComment() != null) {
            PostComment parent = postComment.getParentComment();
            parent.getChildComments().remove(postComment); // 부모 댓글에서 자식 댓글 제거

            if (parent.getChildComments().isEmpty() && parent.getCommentIsDeleted()) {
                postCommentRepository.delete(parent); // 자식도 없고 부모도 soft delete 된 경우
            }
        }
        else{
            postComment.deleteComment();
        }
    }
    private User getCurrentUser() {
        UUID userId = AuthUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
