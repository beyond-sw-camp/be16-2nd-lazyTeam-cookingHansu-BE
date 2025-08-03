package lazyteam.cooking_hansu.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DashBoardResDto {
    private Long waitingLectures; // 승인 대기중인 강의
    private Long waitingApprovalUsers; // 승인 대기중인 사용자
    private Long totalLectures; // 전체 강의 수
    private Long totalUsers; // 전체 사용자 수
}
