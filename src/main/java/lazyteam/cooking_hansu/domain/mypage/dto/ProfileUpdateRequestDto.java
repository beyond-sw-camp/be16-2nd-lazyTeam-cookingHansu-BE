package lazyteam.cooking_hansu.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class ProfileUpdateRequestDto {
    private String nickname;
    private String info;
    private String profileImageUrl;
}
