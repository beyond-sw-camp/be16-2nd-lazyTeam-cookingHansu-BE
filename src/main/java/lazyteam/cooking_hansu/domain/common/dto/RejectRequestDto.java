package lazyteam.cooking_hansu.domain.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RejectRequestDto {
    @NotBlank(message = "거절 사유는 필수 입력입니다.")
    private String reason; // 거절 사유 (REJECTED 상태일 때만 필요)
}
