package lazyteam.cooking_hansu.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRestoreRequestDto {
    private String socialId;
    private String oauthType;
    private String picture; // 새로운 프로필 이미지 URL (선택사항)
}
