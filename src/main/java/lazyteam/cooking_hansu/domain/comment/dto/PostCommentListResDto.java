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
    private String content; // 댓글 내용
    private String authorProfileImage; // 작성자 프로필 이미지 URL
    private String authorNickname; // 작성자 닉네임
    private LocalDateTime createdAt; // 생성일시
    private LocalDateTime updatedAt; // 수정일시
    private List<PostCommentListResDto> childComments; // 대댓글 목록
}
