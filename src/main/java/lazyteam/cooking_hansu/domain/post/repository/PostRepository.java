package lazyteam.cooking_hansu.domain.post.repository;

import io.lettuce.core.dynamic.annotation.Param;
import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findAllByUserAndDeletedAtIsNull(User user);
    @Query("SELECT p FROM Post p " +
            "WHERE p.deletedAt IS NULL " +
            "AND p.isOpen = true " +
            "AND (:role IS NULL OR p.user.role = :role) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "ORDER BY " +
            "CASE WHEN :#{#pageable.sort.toString()} LIKE '%viewCount%' THEN p.viewCount END DESC, " +
            "CASE WHEN :#{#pageable.sort.toString()} LIKE '%likeCount%' THEN p.likeCount END DESC, " +
            "CASE WHEN :#{#pageable.sort.toString()} LIKE '%bookmarkCount%' THEN p.bookmarkCount END DESC, " +
            "CASE WHEN :#{#pageable.sort.toString()} LIKE '%createdAt%' THEN p.createdAt END DESC")
    Page<Post> findPostsByFilters(@Param("role") Role role,
                                  @Param("category") CategoryEnum category,
                                  Pageable pageable);
}
