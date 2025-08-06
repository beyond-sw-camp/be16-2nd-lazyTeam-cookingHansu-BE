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
    @NotNull
    private String orderId;
    @NotNull
    private Long amount;
    @NotNull
    private List<UUID> lectureIds;


    public TossPrePay toEntity() {
        return TossPrePay.builder()
                .amount(amount)
                .orderId(orderId)
                .lectureIds(lectureIds)
                .build();
    }
}
