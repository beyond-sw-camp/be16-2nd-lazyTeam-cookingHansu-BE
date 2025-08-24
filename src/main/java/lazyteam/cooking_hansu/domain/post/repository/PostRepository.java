package lazyteam.cooking_hansu.domain.post.repository;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findAllByUser(User user);
    
    // 팀메이트 스타일: 간단한 기본 조회 메서드들
    Page<Post> findByDeletedAtIsNullAndIsOpenTrue(Pageable pageable);
    
    Page<Post> findByDeletedAtIsNullAndIsOpenTrueAndCategory(CategoryEnum category, Pageable pageable);
}
