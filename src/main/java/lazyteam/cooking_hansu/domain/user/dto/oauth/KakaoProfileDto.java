package lazyteam.cooking_hansu.domain.user.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오 프로필 정보 DTO
 * 카카오 OAuth 인증 후 사용자 프로필 정보 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoProfileDto {
    // 카카오 프로필 정보
    private String id;
    private KakaoAccount kakao_account;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoAccount {
        private String email;
//        private String name;
        private Profile profile;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String profile_image_url;
        private String nickname;
    }
}
