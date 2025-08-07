package lazyteam.cooking_hansu.domain.user.dto.response;

import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 회원가입 상태 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationStatusResDto {

    private boolean isNewUser;
    private boolean isStep1Completed;
    private boolean isStep2Completed;
    private Role currentRole;
    private String nickname;
    private String nextStepMessage;
}
