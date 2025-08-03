package lazyteam.cooking_hansu.domain.user.dto;

import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WaitingBusinessListDto {
    private UUID id;
    private String nickname; // 사용자 닉네임
    private String businessName; // 사업자 이름
    private String businessNumber; // 사업자 등록 번호
    private String businessImageUrl; // 사업자 등록증 이미지 URL
    private String businessAddress; // 사업자 주소
    private String businessType; // 사업자 유형 (예: 식당, 카페 등)
    private String ownerName; // 사업자 소유자 이름
    private LocalDateTime createdAt; // 생성일시

    public static WaitingBusinessListDto fromEntity(Business business) {
        return WaitingBusinessListDto.builder()
                .id(business.getId())
                .nickname(business.getUser().getNickname())
                .businessName(business.getBusinessName())
                .businessNumber(business.getBusinessNumber())
                .businessImageUrl(business.getBusinessUrl())
                .businessAddress(business.getBusinessAddress())
                .businessType(business.getShopCategory())
                .ownerName(business.getUser().getName())
                .createdAt(business.getCreatedAt())
                .build();
    }
}
