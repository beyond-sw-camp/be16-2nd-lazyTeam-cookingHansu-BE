package lazyteam.cooking_hansu.domain.board.repository;

import lazyteam.cooking_hansu.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board , Long> {

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
