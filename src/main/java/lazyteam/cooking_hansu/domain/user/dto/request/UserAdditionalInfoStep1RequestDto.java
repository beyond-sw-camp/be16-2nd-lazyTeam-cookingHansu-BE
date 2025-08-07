package lazyteam.cooking_hansu.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 1단계 추가 정보 입력 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAdditionalInfoStep1RequestDto {

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
    private String nickname;

    @NotNull(message = "역할 선택은 필수입니다")
    private Role role;
}
