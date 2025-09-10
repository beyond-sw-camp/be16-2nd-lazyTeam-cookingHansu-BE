package lazyteam.cooking_hansu.domain.report.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.report.entity.Report;
import lazyteam.cooking_hansu.domain.report.entity.ReportReasonType;
import lazyteam.cooking_hansu.domain.report.entity.ReportType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ReportCreateDto {

    @Enumerated(EnumType.STRING)
    @NotNull(message = "신고 유형은 필수 입력입니다.")
    private ReportType reportType; // 신고 유형 (예: 레시피, 사용자, 댓글 등)
    private UUID targetId; // 신고 대상 ID (예: 레시피 ID, 사용자 ID, 댓글 ID)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "신고 사유 유형은 필수 입력입니다.")
    private ReportReasonType reportReasonType; // 신고 사유 유형 (예: 스팸, 잘못된 내용, 불쾌한 행동 등)
    private String content; // 신고 사유에 대한 추가 설명

//    파라미터에 신고자를 넣어줘야함.
    public Report toEntity(User user) {
        return Report.builder()
                .reportType(this.reportType)
                .targetId(this.targetId)
                .reportReasonType(this.reportReasonType)
                .content(this.content)
                .user(user) // 신고자를 설정하기 위해서는 User 엔티티를 가져와야함
                .build();
    }

}
