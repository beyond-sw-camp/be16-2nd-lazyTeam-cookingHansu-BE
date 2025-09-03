package lazyteam.cooking_hansu.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostCommentListResDto {
    private UUID commentId; // 댓글 ID
    private UUID postId; // 게시물 ID
    private UUID authorId; // 작성자 ID
    private String authorEmail; // 작성자 이메일
    private String authorNickName; // 작성자 닉네임
    private String authorProfileImage; // 작성자 프로필 이미지 URL
    private LocalDateTime authorCreatedAt; // 작성자 가입일
    private String content; // 댓글 내용
    private LocalDateTime createdAt; // 생성일시
    private LocalDateTime updatedAt; // 수정일시
    private Boolean isDeleted; // 삭제 여부
    private List<PostCommentChildListResDto> childComments; // 대댓글 목록
}
