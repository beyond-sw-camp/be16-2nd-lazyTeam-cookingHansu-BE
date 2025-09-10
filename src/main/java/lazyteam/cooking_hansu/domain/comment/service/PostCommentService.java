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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import lazyteam.cooking_hansu.domain.notification.service.NotificationService;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lazyteam.cooking_hansu.domain.notification.entity.TargetType;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

//    댓글 생성
    public UUID createComment(PostCommentCreateDto postCommentCreateDto) {

        // 유저가 존재하는지 확인
        UUID userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postCommentCreateDto.getPostId()).orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));
        PostComment postComment;
        
        // 대댓글인 경우 부모 댓글을 찾아서 설정
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
            
            // 답글 알림 생성 (최초 댓글 작성자에게)
            if (!parentComment.getUser().getId().equals(userId)) {
                notificationService.createAndDispatch(
                    SseMessageDto.builder()
                        .recipientId(parentComment.getUser().getId())
                        .content(user.getNickname() + "님이 회원님의 댓글에 답글을 남겼습니다.")
                        .targetType(TargetType.REPLY)
                        .targetId(post.getId()) // 게시글 ID로 변경
                        .build()
                );
            }
        }
        else {
            postComment = PostComment.builder()
                    .post(post)
                    .user(user)
                    .content(postCommentCreateDto.getContent())
                    .build();
            
            // 게시글 댓글 알림 생성 (게시글 작성자에게)
            if (!post.getUser().getId().equals(userId)) {
                notificationService.createAndDispatch(
                    SseMessageDto.builder()
                        .recipientId(post.getUser().getId())
                        .content(user.getNickname() + "님이 회원님의 게시글에 댓글을 남겼습니다.")
                        .targetType(TargetType.POSTCOMMENT)
                        .targetId(post.getId())
                        .build()
                );
            }
        }
        postCommentRepository.save(postComment);
        return postComment.getId();
    }

//    댓글 목록 조회 (페이지네이션)
    @Transactional(readOnly = true)
    public Page<PostCommentListResDto> findCommentList(UUID postId, Pageable pageable) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        Page<PostComment> commentsPage = postCommentRepository.findAllByPostAndParentCommentIsNull(post, pageable);
        
        return commentsPage.map(c -> PostCommentListResDto.builder()
                .commentId(c.getId())
                .postId(c.getPost().getId())
                .authorId(c.getUser().getId())
                .authorEmail(c.getUser().getEmail())
                .authorNickName(c.getUser().getNickname())
                .authorProfileImage(c.getUser().getPicture())
                .authorCreatedAt(c.getUser().getCreatedAt())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .isDeleted(c.getCommentIsDeleted())
                .childComments(c.getChildComments().stream().map(child -> PostCommentChildListResDto.builder()
                        .commentId(child.getId())
                        .postId(child.getPost().getId())
                        .authorId(child.getUser().getId())
                        .authorEmail(child.getUser().getEmail())
                        .authorNickName(child.getUser().getNickname())
                        .authorProfileImage(child.getUser().getPicture())
                        .authorCreatedAt(child.getUser().getCreatedAt())
                        .content(child.getContent())
                        .createdAt(child.getCreatedAt())
                        .updatedAt(child.getUpdatedAt())
                        .build()).toList())
                .build());
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
}
