package lazyteam.cooking_hansu.domain.comment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Comment extends BaseIdAndTimeEntity {

    // 부모 댓글 (자기참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_parent_id")
    private Comment parentComment;

//    // 대댓글 목록
//    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Comment> childComments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; //게시물 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; //작성자 ID

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 1000, message = "댓글 내용은 1000자 이하여야 합니다")
    @Column(name = "comment_content", nullable = false , columnDefinition = "TEXT")
    private String content;

    @Column(name = "comment_deleted_at")
    private LocalDateTime commentDeletedAt;

    @Builder.Default
    @Column(name = "comment_is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean commentIsDeleted =false;

//    댓글 좋아요 기능 구현 해야되는데 ERD엔 없긴함
    @Builder.Default
    @Column(name = "like_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer likeCount = 0;

//    비즈니스 메서드

//    댓글 수정
    public void updateContent(String content){
        this.content = content;
    }

//    댓글 삭제
    public void softDelete(){
        this.commentIsDeleted = true;
        this.commentDeletedAt = LocalDateTime.now();
        this.content ="삭제된 댓글입니다.";
    }

//    좋아요 수 증가 / 김소
    public void incrementLikeCount() {this.likeCount++;}

    public void decrementLikeCount() {
        if(this.likeCount>0){
            this.likeCount--;
        }
    }

////    본인 확인
//    public boolean isOwner(Long userId){return this.userId.equals(userId);}
//
////    대댓글 여부
//    public boolean isReply() {return this.commentParentId != null;}
//
////    삭제 여부
//    public boolean isDeleted(){return this.commentIsDeleted != null && this.commentIsDeleted;}
}
