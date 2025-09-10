package lazyteam.cooking_hansu.domain.user.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 네이버 프로필 정보 DTO
 * 네이버 OAuth 인증 후 사용자 프로필 정보 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaverProfileDto {
    private String resultcode;
    private String message;
    private Response response;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id; // 동일인 식별 정보 (고유 번호)
        private String name; // 이름
        private String email; // 이메일
        private String profile_image; // 프로필 이미지 URL
    }
}
