package lazyteam.cooking_hansu.domain.report.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.common.dto.Status;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
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

    // Report ID (자동 생성)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Report Type (RECIPE, USER, COMMENT)
    @NotNull(message = "신고 유형은 필수 선택입니다.")
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    // Target ID (예: Recipe ID, User ID, Comment ID)
    @NotNull(message = "신고 대상 ID는 필수 입력입니다.")
    private Long targetId;

    // Report Reason Type (신고 사유 타입: SPAM_OR_ADS, INCORRECT_CONTENTS, BOTHER_OR_SPIT, FRAUD_INFORMATION, AUTHORIZATION, ETC)
    @NotNull(message = "신고 사유 타입은 필수 선택입니다.")
    @Enumerated(EnumType.STRING)
    private ReportReasonType reportReasonType;

    // Report Reason (신고 사유 내용)
    private String content;

    // Report Status (신고 상태: PENDING, APPROVED, REJECTED)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;

    // 거절 사유
    private String rejectReason;

    // 신고한 회원ID (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User member;

    // 신고를 처리한 관리자ID (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;


    public void updateStatus(Status status, String statusReason) {
        this.status = status;
        this.rejectReason = statusReason;
    }
}
