package lazyteam.cooking_hansu.domain.post.repository;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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

    // 기본조회 - 삭제안한거 공개한것만 찾기
    Page<Post> findByIsOpenTrueAndDeletedAtIsNull(Pageable pageable);

    // 검색 - 삭제안한거 공개한것 중에서 제목으로 검색
    Page<Post> findByIsOpenTrueAndDeletedAtIsNullAndTitleContaining(String title, Pageable pageable);

    // 사용자별 조회
    Page<Post> findByUserAndDeletedAtIsNull(User user, Pageable pageable);
    List<Post> findByUserAndDeletedAtIsNull(User user);

    // 특정 사용자의 공개 게시글만 조회
    Page<Post> findByUserAndIsOpenTrueAndDeletedAtIsNull(User user, Pageable pageable);

    // 카테고리별 조회 (공개된 것만)
    Page<Post> findByCategoryAndIsOpenTrueAndDeletedAtIsNull(CategoryEnum category, Pageable pageable);

    // 카테고리별 조회 (전체 - 관리자용)
    Page<Post> findByCategoryAndDeletedAtIsNull(CategoryEnum category, Pageable pageable);
}
