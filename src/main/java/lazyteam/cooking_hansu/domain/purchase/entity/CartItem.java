package lazyteam.cooking_hansu.domain.purchase.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.User;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lombok.*;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    // 강의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}