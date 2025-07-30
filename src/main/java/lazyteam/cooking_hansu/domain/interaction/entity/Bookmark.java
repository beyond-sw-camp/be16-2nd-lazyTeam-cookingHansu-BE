package lazyteam.cooking_hansu.domain.interaction.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lombok.*;

// 북마크 엔티티
// 김상환 담당 - REQ029 (북마크 추가/취소)
@Entity
@Table(name = "Bookmark",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Bookmark extends BaseIdAndTimeEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId; // 사용자 ID

    @Column(name = "post_id", nullable = false)
    private Long postId; // 게시글 ID
}
