package lazyteam.cooking_hansu.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.user.entity.chef.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요식업 종사자 2단계 추가 정보 입력 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChefUserStep2RequestDto {

    @NotBlank(message = "자격 번호는 필수입니다")
    private String licenseNumber;

    @NotNull(message = "자격 업종 선택은 필수입니다")
    private CuisineType cuisineType;

    @NotBlank(message = "자격증 이미지 URL은 필수입니다")
    private String licenseUrl;
}
