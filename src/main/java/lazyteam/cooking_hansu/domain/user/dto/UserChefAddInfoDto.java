package lazyteam.cooking_hansu.domain.user.dto;

import lazyteam.cooking_hansu.domain.user.entity.chef.CuisineType;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 추가 정보 DTO
 * 요식업 종사자 전용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChefAddInfoDto {

    private String nickname; // 닉네임
    private Role role = Role.CHEF; // 역활 (Chef 기본)
    private String licenseNumber; // 자격 번호
    private CuisineType cuisineType; // 자격 업종
    private String licenseUrl; // 자격증 이미지 url 
}
