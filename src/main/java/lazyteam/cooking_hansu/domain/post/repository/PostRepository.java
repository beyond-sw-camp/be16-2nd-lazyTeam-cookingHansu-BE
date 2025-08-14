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
    
    // === 레시피 공유 서비스용 추가 메서드 ===
    
    // 사용자별 조회 (생성일 내림차순 정렬)
    Page<Post> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 제목과 설명에서 키워드 검색 (공개된 것만)
    Page<Post> findByIsOpenTrueAndDeletedAtIsNullAndTitleContainingOrDescriptionContaining(
            String titleKeyword, String descriptionKeyword, Pageable pageable);
    
    // 카테고리 + 키워드 검색 (공개된 것만)
    Page<Post> findByCategoryAndIsOpenTrueAndDeletedAtIsNullAndTitleContainingOrDescriptionContaining(
            CategoryEnum category, String titleKeyword, String descriptionKeyword, Pageable pageable);
}
