package lazyteam.cooking_hansu.domain.user.dto;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoProfileDto {
    // 카카오 프로필 정보
    private String id;
    private KakaoAccount kakao_account;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String email;
        private Profile profile;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String nickname;
        private String profile_image_url;
        private String name;
    }

    // 카카오 프로필을 공통 프로필로 변환
    public CommonProfileDto toCommonProfile() {
        String email = kakao_account != null ? kakao_account.getEmail() : null;
        String name = kakao_account != null && kakao_account.getProfile() != null
            ? kakao_account.getProfile().getNickname() : null;
        String picture = kakao_account != null && kakao_account.getProfile() != null
            ? kakao_account.getProfile().getProfile_image_url() : null;

        return new CommonProfileDto(id, name, email, picture);
    }
}
