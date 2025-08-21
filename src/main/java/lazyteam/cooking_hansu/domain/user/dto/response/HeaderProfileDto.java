package lazyteam.cooking_hansu.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인한 상태에서 헤더에 표시될 프로필 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeaderProfileDto {

    private String nickname;
    private String profileImageUrl;
}
