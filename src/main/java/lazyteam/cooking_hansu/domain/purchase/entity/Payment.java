package lazyteam.cooking_hansu.domain.purchase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.PayMethod;
import lazyteam.cooking_hansu.domain.common.PaymentStatus;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseIdAndTimeEntity {

    // 사용자 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 총 결제 금액
    @NotNull(message = "결제 금액은 필수입니다")
    @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다")
    @Column(nullable = false)
    private Integer paidAmount;

    // 결제 수단
    @NotNull(message = "결제 수단은 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayMethod payMethod;

    // 결제 상태
    @NotNull(message = "결제 상태는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // 실제 결제 완료 시각
    @NotNull(message = "결제 완료 시각은 필수입니다")
    @Column(nullable = false)
    private LocalDateTime paidAt;

    // 역방향 관계설정(조회용), 구매된강의와 연결
    @OneToMany(mappedBy = "payment")
    private List<PurchasedLecture> purchasedLectures;
}