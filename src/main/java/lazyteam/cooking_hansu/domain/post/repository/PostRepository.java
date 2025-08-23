package lazyteam.cooking_hansu.domain.post.repository;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findAllByUser(User user);

    Page<Post> findByIsOpenTrueAndDeletedAtIsNull(Pageable pageable);

    Page<Post> findByUserAndDeletedAtIsNull(User user, Pageable pageable);

    Page<Post> findByUserAndIsOpenTrueAndDeletedAtIsNull(User user, Pageable pageable);

    Page<Post> findByCategoryAndIsOpenTrueAndDeletedAtIsNull(CategoryEnum category, Pageable pageable);

    Page<Post> findByCategoryAndDeletedAtIsNull(CategoryEnum category, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.user u WHERE u.role = :role AND p.isOpen = true AND p.deletedAt IS NULL")
    Page<Post> findByUserRoleAndIsOpenTrueAndDeletedAtIsNull(@Param("role") Role role, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.user u WHERE p.category = :category AND u.role = :role AND p.isOpen = true AND p.deletedAt IS NULL")
    Page<Post> findByCategoryAndUserRoleAndIsOpenTrueAndDeletedAtIsNull(@Param("category") CategoryEnum category, @Param("role") Role role, Pageable pageable);
}