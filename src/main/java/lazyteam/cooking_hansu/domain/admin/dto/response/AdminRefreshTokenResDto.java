package lazyteam.cooking_hansu.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRefreshTokenResDto {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
