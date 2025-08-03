package lazyteam.cooking_hansu.domain.interaction.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lombok.*;

//      좋아요 엔티티
@Entity
//      복합키 설정
@Table(name = "Likes",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","post_id"}))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Likes extends BaseIdAndTimeEntity {

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;
}
