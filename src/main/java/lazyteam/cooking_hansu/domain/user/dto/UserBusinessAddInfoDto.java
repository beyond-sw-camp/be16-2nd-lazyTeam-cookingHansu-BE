package lazyteam.cooking_hansu.domain.user.dto;

import jakarta.persistence.Column;
import lazyteam.cooking_hansu.domain.user.entity.chef.CuisineType;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 추가 정보 DTO
 * 요식업 자영업자 전용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBusinessAddInfoDto {

    private String nickname; // 닉네임
    private Role role = Role.OWNER; // 역활 (Chef 기본)
    private String businessNumber; // 사업자 등록 번호
    private String businessUrl; // 사업자 등록증 파일 url
    private String businessName; // 상호명
    private String businessAddress; // 사업지 주소
    private String shopCategory; // 사업 업종
}
