package lazyteam.cooking_hansu.domain.user.dto;

import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.chef.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WaitingChefListDto {
    private UUID userId; // 사용자 ID
    private String nickname; // 사용자 닉네임
    private String imageUrl; // 프로필 이미지 URL
    private String LicenseNumber; // 요리사 면허 번호
    private String LicenseImageUrl; // 요리사 면허 이미지 URL
    private CuisineType cuisineType; // 요리사 전문 분야

    public static WaitingChefListDto fromEntity(Chef chef) {
        return WaitingChefListDto.builder()
                .userId(chef.getUser().getId())
                .nickname(chef.getUser().getNickname())
                .imageUrl(chef.getUser().getPicture())
                .LicenseNumber(chef.getLicenseNumber())
                .LicenseImageUrl(chef.getLicenseUrl())
                .cuisineType(chef.getCuisineType())
                .build();
    }
}
