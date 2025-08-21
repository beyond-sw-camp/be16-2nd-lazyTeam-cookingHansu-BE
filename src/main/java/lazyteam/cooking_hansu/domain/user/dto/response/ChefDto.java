package lazyteam.cooking_hansu.domain.user.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.chef.CuisineType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChefDto {

    private User user;

    @NotBlank(message = "자격 번호는 필수입니다.")
    private String licenseNumber;

    @NotNull(message = "자격 업종 선택은 필수입니다.")
    private CuisineType cuisineType;

    private String licenseUrl;

    public Chef toEntity() {
        return Chef.builder()
                .user(this.user)
                .licenseNumber(this.getLicenseNumber())
                .cuisineType(this.getCuisineType())
                .licenseUrl(this.getLicenseUrl())
                .build();
    }

    public static ChefDto fromEntity(Chef chef) {
        return ChefDto.builder()
                .user(chef.getUser())
                .licenseNumber(chef.getLicenseNumber())
                .cuisineType(chef.getCuisineType())
                .licenseUrl(chef.getLicenseUrl())
                .build();
    }
}
