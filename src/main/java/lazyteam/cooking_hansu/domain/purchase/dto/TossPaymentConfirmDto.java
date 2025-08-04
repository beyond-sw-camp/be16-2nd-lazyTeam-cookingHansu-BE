package lazyteam.cooking_hansu.domain.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}