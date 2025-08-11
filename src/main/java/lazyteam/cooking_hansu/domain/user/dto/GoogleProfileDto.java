package lazyteam.cooking_hansu.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구글 프로필 정보 DTO
 * 구글 OAuth 인증 후 사용자 프로필 정보 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleProfileDto {
    // 구글 프로필 정보
    private String sub; // 구글에서 사용하는 고유 사용자 ID
    private String name;
    private String email;
    private String picture;
    private String given_name;
    private String family_name;
    private String locale;

    // 구글 프로필을 공통 프로필로 변환
    public CommonProfileDto toCommonProfile() {
        return new CommonProfileDto(sub, name, email, picture);
    }
}
