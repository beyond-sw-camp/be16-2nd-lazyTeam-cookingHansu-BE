package lazyteam.cooking_hansu.domain.report.dto;


import lazyteam.cooking_hansu.domain.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ReportDetailDto {
    private Long id;
    private String reportType; // 신고 유형
    private Long targetId; // 신고 대상 ID
    private String reportReasonType; // 신고 사유 유형
    private String content; // 신고 내용
    private Long reporterId; // 신고자 ID

    public static ReportDetailDto fromEntity(Report report, Long reporterId) {
        return ReportDetailDto.builder()
                .id(report.getId())
                .reportType(report.getReportType().name())
                .targetId(report.getTargetId())
                .reportReasonType(report.getReportReasonType().name())
                .content(report.getContent())
                .reporterId(reporterId)
                .build();
    }
}
