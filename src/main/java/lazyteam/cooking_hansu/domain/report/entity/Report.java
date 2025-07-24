package lazyteam.cooking_hansu.domain.report.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lazyteam.cooking_hansu.domain.common.dto.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Report ID (자동 생성)
    private ReportType reportType; // Report Type (RECIPE, USER, COMMENT)
    private Long targetId; // Target ID (예: Recipe ID, User ID, Comment ID)
    private ReportReasonType reportReasonType; // Report Reason Type (신고 사유 타입: SPAM_OR_ADS, INCORRECT_CONTENTS, BOTHER_OR_SPIT, FRAUD_INFORMATION, AUTHORIZATION, ETC)
    private String content; // Report Reason (신고 사유 내용)
    private String description; // Report Description (신고 내용)
    private Status status; // Report Status (신고 상태: PENDING, APPROVED, REJECTED)

    private Long reporterId; // Reporter ID (신고자 ID)
}
