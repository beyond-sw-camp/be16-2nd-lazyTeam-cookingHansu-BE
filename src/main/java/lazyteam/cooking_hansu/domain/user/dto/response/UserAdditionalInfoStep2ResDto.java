package lazyteam.cooking_hansu.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 2단계 추가 정보 입력 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdditionalInfoStep2ResDto {

    private String message;
    private boolean isRegistrationCompleted;
}
