package lazyteam.cooking_hansu.domain.purchase.dto;

import lazyteam.cooking_hansu.domain.purchase.entity.CartItem;
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

public class CartItemListDto {

    private UUID lectureId;
    private String lectureTitle;
    private UUID userId;
    private Integer price;
    private String writerNickName;

    public static CartItemListDto fromEntity(CartItem cartItem) {
        return CartItemListDto.builder()
                .lectureId(cartItem.getLecture().getId())
                .lectureTitle(cartItem.getLecture().getTitle())
                .userId(cartItem.getUser().getId())
                .price(cartItem.getLecture().getPrice())
                .writerNickName(cartItem.getLecture().getSubmittedBy().getNickname())
                .build();

    }

}
