package lazyteam.cooking_hansu.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RejectRequestDto {
    private String reason; // 거절 사유 (REJECTED 상태일 때만 필요)
}
