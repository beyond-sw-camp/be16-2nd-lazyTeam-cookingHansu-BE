package lazyteam.cooking_hansu.domain.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class TossPaymentConfirmDto {
    @NotBlank
    private String paymentKey;
    @NotBlank
    private String orderId;
    @NotBlank
    private String amount;
    @NotNull(message = "결제할 강의 목록이 필요합니다.")
    private List<UUID> lectureIds;
}