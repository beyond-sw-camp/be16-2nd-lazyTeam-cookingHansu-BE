package lazyteam.cooking_hansu.domain.interaction.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

/**
 * 강의 좋아요 엔티티 (ERD 대로 별도 테이블)
 */
@Entity
@Table(name = "Lecture_Likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lecture_id"}))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString(exclude = {"user", "lecture"})
public class LectureLikes extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;
}
