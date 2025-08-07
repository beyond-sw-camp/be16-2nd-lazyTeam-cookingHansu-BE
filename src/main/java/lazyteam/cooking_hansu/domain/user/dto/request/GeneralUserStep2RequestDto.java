package lazyteam.cooking_hansu.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.user.entity.common.GeneralType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일반 회원 2단계 추가 정보 입력 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralUserStep2RequestDto {

    @NotNull(message = "일반 회원 유형 선택은 필수입니다")
    private GeneralType generalType;
}
