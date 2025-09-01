package lazyteam.cooking_hansu.domain.user.dto.response;

import jakarta.validation.constraints.NotBlank;
import lazyteam.cooking_hansu.domain.user.entity.business.Owner;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessDto {

    @NotBlank(message = "사업자 등록 번호는 필수입니다.")
    private String businessNumber;

    private String businessUrl;

    @NotBlank(message = "상호명은 필수입니다.")
    private String businessName;

    @NotBlank(message = "사업지 주소는 필수입니다.")
    private String businessAddress;

    @NotBlank(message = "사업 업종은 필수입니다.")
    private String shopCategory;

    public static BusinessDto fromEntity(Owner owner) {
        return BusinessDto.builder()
                .businessNumber(owner.getBusinessNumber())
                .businessUrl(owner.getBusinessUrl())
                .businessName(owner.getBusinessName())
                .businessAddress(owner.getBusinessAddress())
                .shopCategory(owner.getShopCategory())
                .build();
    }
}