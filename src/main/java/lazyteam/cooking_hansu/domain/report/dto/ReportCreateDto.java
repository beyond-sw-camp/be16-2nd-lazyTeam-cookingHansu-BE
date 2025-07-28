package lazyteam.cooking_hansu.domain.report.dto;

import lazyteam.cooking_hansu.domain.report.entity.Report;
import lazyteam.cooking_hansu.domain.report.entity.ReportReasonType;
import lazyteam.cooking_hansu.domain.report.entity.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ReportCreateDto {
    private ReportType reportType; // 신고 유형 (예: 레시피, 사용자, 댓글 등)
    private Long targetId; // 신고 대상 ID (예: 레시피 ID, 사용자 ID, 댓글 ID)
    private ReportReasonType reportReasonType; // 신고 사유 유형 (예: 스팸, 잘못된 내용, 불쾌한 행동 등)
    private String content; // 신고 사유에 대한 추가 설명
    private Long reporterId; // 신고를 한 사용자의 ID

//    파라미터에 신고자를 넣어줘야함.
    public Report toEntity(Long reporterId) {
        return Report.builder()
                .reportType(this.reportType)
                .targetId(this.targetId)
                .reportReasonType(this.reportReasonType)
                .content(this.content)
                .reporterId(reporterId)
                .build();
    }

}
