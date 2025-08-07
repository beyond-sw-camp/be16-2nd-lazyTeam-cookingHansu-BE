package lazyteam.cooking_hansu.domain.user.dto.response;

import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 1단계 추가 정보 입력 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdditionalInfoStep1ResDto {

    private String message;
    private String nickname;
    private Role role;
    private boolean isStep1Completed;
}
