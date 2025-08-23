package lazyteam.cooking_hansu.domain.interaction.repository;

import lazyteam.cooking_hansu.domain.interaction.entity.Bookmark;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
    List<Bookmark> findAllByUser(User user);
    
    Optional<Bookmark> findByUserAndPost(User user, Post post);
    
    boolean existsByUserAndPost(User user, Post post);
    
    long countByPost(Post post);
    
    // UUID로 직접 조회하는 메서드 추가
    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId AND b.post.id = :postId")
    Bookmark findByUserIdAndPostId(@Param("userId") UUID userId, @Param("postId") UUID postId);
    
    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.post.id = :postId")
    long countByPostId(@Param("postId") UUID postId);

    @Query("DELETE FROM Bookmark b WHERE b.post.id = :postId")
    void deleteByPostId(@Param("postId") UUID postId);
}
