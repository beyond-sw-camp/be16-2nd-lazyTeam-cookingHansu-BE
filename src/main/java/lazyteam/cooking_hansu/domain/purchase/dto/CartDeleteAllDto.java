package lazyteam.cooking_hansu.domain.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartDeleteAllDto {
    private UUID userId;
}
