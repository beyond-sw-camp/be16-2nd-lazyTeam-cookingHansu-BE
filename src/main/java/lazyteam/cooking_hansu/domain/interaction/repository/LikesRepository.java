package lazyteam.cooking_hansu.domain.interaction.repository;

import lazyteam.cooking_hansu.domain.interaction.entity.Likes;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LikesRepository extends JpaRepository<Likes, UUID> {
    List<Likes> findAllByUser(User user);

    
    // UUID로 직접 조회하는 메서드 추가
    Likes findByUserIdAndPostId(@Param("userId") UUID userId, @Param("postId") UUID postId);

    long countByPostId(@Param("postId") UUID postId);
}
