package lazyteam.cooking_hansu.domain.post.dto;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostFilterRequestDto {

    private CategoryEnum category;
    private Role userRole;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "desc";

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    public boolean isValidSortBy() {
        return sortBy != null && sortBy.equals("createdAt");
    }

    public boolean isValidSortDirection() {
        return sortDirection != null && (sortDirection.equals("asc") || sortDirection.equals("desc"));
    }

    public boolean hasCategoryFilter() {
        return category != null;
    }

    public boolean hasUserRoleFilter() {
        return userRole != null;
    }
}