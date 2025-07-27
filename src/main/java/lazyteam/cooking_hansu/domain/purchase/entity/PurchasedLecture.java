package lazyteam.cooking_hansu.domain.purchase.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.User;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lombok.*;

@Entity
@Table(name = "purchased_lecture")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasedLecture extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchasedId;

    // 회원 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 결제 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // 강의 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // 결제 시점 강의 제목
    @Column(length = 255, nullable = false)
    private String lectureTitleSnapshot;

    // 결제 시점 가격
    @Column(nullable = false)
    private Integer priceSnapshot;


}
