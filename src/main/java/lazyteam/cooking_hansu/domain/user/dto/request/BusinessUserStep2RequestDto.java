package lazyteam.cooking_hansu.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요식업 자영업자 2단계 추가 정보 입력 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUserStep2RequestDto {

    @NotBlank(message = "사업자 등록 번호는 필수입니다")
    private String businessNumber;

    @NotBlank(message = "사업자 등록증 파일 URL은 필수입니다")
    private String businessUrl;

    @NotBlank(message = "상호명은 필수입니다")
    private String businessName;

    @NotBlank(message = "사업지 주소는 필수입니다")
    private String businessAddress;

    @NotBlank(message = "사업 업종은 필수입니다")
    private String shopCategory;
}
