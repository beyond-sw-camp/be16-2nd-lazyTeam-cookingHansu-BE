package lazyteam.cooking_hansu.domain.interaction.repository;

import lazyteam.cooking_hansu.domain.interaction.entity.LectureLikes;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 강의 좋아요 Repository
 */
@Repository
public interface LectureLikesRepository extends JpaRepository<LectureLikes, UUID> {

    /**
     * 사용자가 특정 강의에 좋아요를 눌렀는지 확인
     */
    boolean existsByUserAndLecture(User user, Lecture lecture);

    /**
     * 사용자와 강의로 좋아요 정보 조회
     */
    Optional<LectureLikes> findByUserAndLecture(User user, Lecture lecture);

    /**
     * 특정 강의의 좋아요 개수 조회
     */
    long countByLecture(Lecture lecture);

    /**
     * 특정 사용자가 좋아요한 강의 목록 조회 (페이징)
     */
    @Query("SELECT ll FROM LectureLikes ll " +
           "JOIN FETCH ll.lecture l " +
           "WHERE ll.user = :user " +
           "ORDER BY ll.createdAt DESC")
    Page<LectureLikes> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    /**
     * 강의 삭제시 관련 좋아요 정보도 함께 삭제
     */
    void deleteByLecture(Lecture lecture);

    /**
     * 사용자 탈퇴시 관련 좋아요 정보 삭제
     */
    void deleteByUser(User user);
    
    // UUID로 직접 조회하는 메서드 추가
    @Query("SELECT ll FROM LectureLikes ll WHERE ll.user.id = :userId AND ll.lecture.id = :lectureId")
    LectureLikes findByUserIdAndLectureId(@Param("userId") UUID userId, @Param("lectureId") UUID lectureId);
    
    @Query("SELECT COUNT(ll) FROM LectureLikes ll WHERE ll.lecture.id = :lectureId")
    long countByLectureId(@Param("lectureId") UUID lectureId);
}
