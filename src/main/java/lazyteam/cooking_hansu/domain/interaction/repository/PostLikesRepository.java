package lazyteam.cooking_hansu.domain.interaction.repository;

import lazyteam.cooking_hansu.domain.interaction.entity.PostLikes;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostLikesRepository extends JpaRepository<PostLikes, UUID> {
    Page<PostLikes> findAllByUser(User user, Pageable pageable);

    
    // UUID로 직접 조회하는 메서드 추가
    PostLikes findByUserIdAndPostId(@Param("userId") UUID userId, @Param("postId") UUID postId);

    long countByPostId(@Param("postId") UUID postId);
}
