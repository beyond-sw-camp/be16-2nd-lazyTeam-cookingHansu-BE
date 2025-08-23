package lazyteam.cooking_hansu.domain.post.dto;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lombok.*;

/**
 * 레시피 공유 게시글 필터 요청 DTO
 * 검색 기능 없이 필터링만 지원
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostFilterRequestDto {

    // ========== 필터 기능만 ==========
    
    private CategoryEnum category; // 카테고리 필터 (한식, 중식, 양식, 일식)
    private Role userRole; // 작성자 역할 필터 (일반사용자, 셰프, 자영업자)
    
    @Builder.Default
    private String sortBy = "createdAt"; // 정렬 기준 (createdAt만 지원)
    
    @Builder.Default
    private String sortDirection = "desc"; // 정렬 방향 (asc, desc)
    
    @Builder.Default
    private Integer page = 0; // 페이지 번호 (0부터 시작)
    
    @Builder.Default
    private Integer size = 10; // 페이지 크기

    /**
     * 정렬 기준 유효성 검증
     */
    public boolean isValidSortBy() {
        return sortBy != null && sortBy.equals("createdAt");
    }

    /**
     * 정렬 방향 유효성 검증
     */
    public boolean isValidSortDirection() {
        return sortDirection != null && 
               (sortDirection.equals("asc") || sortDirection.equals("desc"));
    }

    /**
     * 카테고리 필터 여부
     */
    public boolean hasCategoryFilter() {
        return category != null;
    }

    /**
     * 사용자 역할 필터 여부
     */
    public boolean hasUserRoleFilter() {
        return userRole != null;
    }
}
