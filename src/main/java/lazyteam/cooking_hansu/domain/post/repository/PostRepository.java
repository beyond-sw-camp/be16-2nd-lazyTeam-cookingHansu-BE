package lazyteam.cooking_hansu.domain.post.repository;

import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByUser(User user);
////    기본조회
//
////    삭제되지 않은 게시글만 조회
//    @Query("SELECT b FROM Board b WHERE b.deletedAt IS NULL")
//    Page<Board> findAllNotDeleted(Pageable pageable);
//
////    공개 게시글만 조회
//    @Query("SELECT b FROM Board b WHERE b.deletedAt IS NULL AND b.isOpen = true")
//    Page<Board> findAllPublic(Pageable pageable);
//
////    특정 사용자의 게시글 조회
//    @Query
//    Page<Board> findByUserIdNotDeleted(@Param("userId") Long userId, Pageable pageable);
//
//


}
