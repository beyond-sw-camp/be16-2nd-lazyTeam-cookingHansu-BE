package lazyteam.cooking_hansu.domain.interaction.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

// 북마크 엔티티
@Entity
@Table(name = "Bookmark",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Bookmark extends BaseIdAndTimeEntity {
    // 회원 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 게시글 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;


}
