package lazyteam.cooking_hansu.domain.comment.dto;

import lazyteam.cooking_hansu.domain.comment.entity.PostComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostCommentCreateDto {
    private UUID postId; // 게시글 ID
    private UUID parentCommentId; // 부모 댓글 ID (대댓글의 경우)
    private String content; // 댓글 내용

    public PostCommentCreateDto toEntity(PostComment postComment) {
        return PostCommentCreateDto.builder()
                .postId(postComment.getPost().getId())
                .parentCommentId(postComment.getParentComment() != null ? postComment.getParentComment().getId() : null)
                .content(postComment.getContent())
                .build();
    }
}
