package lazyteam.cooking_hansu.domain.user.dto;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverProfileDto {
    private String resultcode;
    private String message;
    private Response response;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private String id; // 동일인 식별 정보 (고유 번호)
        private String nickname; // 닉네임
        private String name; // 이름
        private String email; // 이메일
        private String profile_image; // 프로필 이미지 URL
    }

    // 네이버 프로필을 공통 프로필로 변환 (CommonProfileDto와 동일한 구조)
    public String getId() {
        return response != null ? response.getId() : null;
    }

    public String getName() {
        return response != null ? response.getName() : null;
    }

    public String getEmail() {
        return response != null ? response.getEmail() : null;
    }

    public String getPicture() {
        return response != null ? response.getProfile_image() : null;
    }
}
