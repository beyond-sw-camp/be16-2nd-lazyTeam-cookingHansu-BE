package lazyteam.cooking_hansu.domain.interaction.repository;

import lazyteam.cooking_hansu.domain.interaction.entity.Bookmark;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
    Page<Bookmark> findAllByUser(User user, Pageable pageable);
    // UUID로 직접 조회하는 메서드 추가
    Bookmark findByUserIdAndPostId(@Param("userId") UUID userId, @Param("postId") UUID postId);

    long countByPostId(@Param("postId") UUID postId);

}
