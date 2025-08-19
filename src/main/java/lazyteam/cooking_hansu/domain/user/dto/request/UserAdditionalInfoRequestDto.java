package lazyteam.cooking_hansu.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.user.entity.chef.CuisineType;
import lazyteam.cooking_hansu.domain.user.entity.common.GeneralType;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 회원 추가 정보 입력 통합 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAdditionalInfoRequestDto {

    // 1단계 공통 정보
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
    private String nickname;

    @NotNull(message = "역할 선택은 필수입니다")
    private Role role;

    // 일반 회원 정보
    private GeneralType generalType;

    // 요식업 종사자 정보
    private String licenseNumber;
    private CuisineType cuisineType;
    private MultipartFile licenseFile; // 자격증 파일

    // 요식업 자영업자 정보
    private String businessNumber;
    private MultipartFile businessFile; // 사업자등록증 파일
    private String businessName;
    private String businessAddress;
    private String shopCategory;
}
