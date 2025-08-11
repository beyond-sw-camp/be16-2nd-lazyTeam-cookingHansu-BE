package lazyteam.cooking_hansu.global.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 필드는 자동 무시
public class CommonTokenDto {
    private String access_token; // 액세스 토큰
    private String refresh_token; // 리프레시 토큰
    private Integer expires_in; // 토큰 만료 시간 (초)
    private String scope; // 권한 범위 (google: openid, email, profile)
    private String id_token; // ID 토큰
}
