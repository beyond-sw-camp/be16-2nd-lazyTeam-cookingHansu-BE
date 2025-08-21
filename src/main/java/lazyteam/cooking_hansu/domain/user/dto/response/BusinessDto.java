package lazyteam.cooking_hansu.domain.user.dto.response;

import jakarta.validation.constraints.NotBlank;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessDto {

    private User user;

    @NotBlank(message = "사업자 등록 번호는 필수입니다.")
    private String businessNumber;

    private String businessUrl;

    @NotBlank(message = "상호명은 필수입니다.")
    private String businessName;

    @NotBlank(message = "사업지 주소는 필수입니다.")
    private String businessAddress;

    @NotBlank(message = "사업 업종은 필수입니다.")
    private String shopCategory;

    public Business toEntity() {
        return Business.builder()
                .user(this.user)
                .businessNumber(this.businessNumber)
                .businessUrl(this.businessUrl)
                .businessName(this.businessName)
                .businessAddress(this.businessAddress)
                .shopCategory(this.shopCategory)
                .build();
    }

    public static BusinessDto fromEntity(Business business) {
        return BusinessDto.builder()
                .user(business.getUser())
                .businessNumber(business.getBusinessNumber())
                .businessUrl(business.getBusinessUrl())
                .businessName(business.getBusinessName())
                .businessAddress(business.getBusinessAddress())
                .shopCategory(business.getShopCategory())
                .build();
    }
}