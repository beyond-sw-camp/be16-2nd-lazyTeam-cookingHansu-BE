package lazyteam.cooking_hansu.domain.report.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.common.dto.Status;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
public class Report extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Report ID (자동 생성)
    @NotNull(message = "신고 유형은 필수 선택입니다.")
    @Enumerated(EnumType.STRING)
    private ReportType reportType; // Report Type (RECIPE, USER, COMMENT)
    @NotNull(message = "신고 대상 ID는 필수 입력입니다.")
    private Long targetId; // Target ID (예: Recipe ID, User ID, Comment ID)
    @NotNull(message = "신고 사유 타입은 필수 선택입니다.")
    @Enumerated(EnumType.STRING)
    private ReportReasonType reportReasonType; // Report Reason Type (신고 사유 타입: SPAM_OR_ADS, INCORRECT_CONTENTS, BOTHER_OR_SPIT, FRAUD_INFORMATION, AUTHORIZATION, ETC)
    private String content; // Report Reason (신고 사유 내용)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING; // Report Status (신고 상태: PENDING, APPROVED, REJECTED)
    private String rejectReason; // 거절 사유

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User member; // 신고한 회원 (Member 엔티티와 연관 관계)

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Administrator admin; // 신고를 처리한 관리자 (Member 엔티티와 연관 관계)


    public void updateStatus(Status status, String statusReason) {
        this.status = status;
        this.rejectReason = statusReason;
    }
}
