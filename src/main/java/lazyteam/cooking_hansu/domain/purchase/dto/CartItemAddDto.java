package lazyteam.cooking_hansu.domain.purchase.dto;

import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.purchase.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CartItemAddDto {

    @NotNull(message = "최소 하나 이상의 강의를 선택해주세요.")
    private List<UUID> lectureIds;

}
