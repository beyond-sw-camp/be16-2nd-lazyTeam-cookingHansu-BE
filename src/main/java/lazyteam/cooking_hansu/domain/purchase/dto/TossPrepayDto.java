package lazyteam.cooking_hansu.domain.purchase.dto;

import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.purchase.entity.TossPrePay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TossPrepayDto {
    @NotNull(message = "주문번호는 필수입니다.")
    private String orderId;
    @NotNull(message = "주문금액은 필수입니다.")
    private Long amount;
    @NotNull(message = "강의ID는 필수입니다.")
    private List<UUID> lectureIds;


    public TossPrePay toEntity() {
        return TossPrePay.builder()
                .amount(amount)
                .orderId(orderId)
                .lectureIds(lectureIds)
                .build();
    }
}
