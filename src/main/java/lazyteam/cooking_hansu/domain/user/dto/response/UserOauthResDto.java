package lazyteam.cooking_hansu.domain.user.dto.response;

import lazyteam.cooking_hansu.domain.user.dto.oauth.NaverProfileDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOauthResDto {

    private String resultcode; // 결과 코드
    private String message; // 결과 메시지
    private NaverProfileDto response; // 네이버 프로필 정보
}
