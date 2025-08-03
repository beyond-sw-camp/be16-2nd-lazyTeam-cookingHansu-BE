package lazyteam.cooking_hansu.domain.post.dto;

import lombok.*;

/**
 * 레시피 공유 게시글 검색 요청 DTO
 * 요구사항 기반 최소 기능만 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSearchRequestDto {

    // ========== 필수 검색 기능만 ==========
    
    private String keyword; // 제목 검색 키워드
    private String sortBy = "createdAt"; // 정렬 기준 (createdAt만 지원)
    private String sortDirection = "desc"; // 정렬 방향 (asc, desc)
    
    @Builder.Default
    private Integer page = 0; // 페이지 번호 (0부터 시작)
    
    @Builder.Default
    private Integer size = 10; // 페이지 크기

    // 작성자 필터 (특정 사용자의 게시글만 조회)
    private String userId;

    /**
     * 검색 조건 유효성 검증
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
     * 키워드 검색 여부
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * 사용자 필터 여부
     */
    public boolean hasUserFilter() {
        return userId != null && !userId.trim().isEmpty();
    }
}
