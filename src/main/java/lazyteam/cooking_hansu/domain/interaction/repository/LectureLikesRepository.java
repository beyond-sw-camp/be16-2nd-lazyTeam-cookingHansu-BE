package lazyteam.cooking_hansu.domain.interaction.repository;

import lazyteam.cooking_hansu.domain.interaction.entity.LectureLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 강의 좋아요 Repository
 */
@Repository
public interface LectureLikesRepository extends JpaRepository<LectureLikes, UUID> {
    
    // UUID로 직접 조회하는 메서드 추가
    LectureLikes findByUserIdAndLectureId(@Param("userId") UUID userId, @Param("lectureId") UUID lectureId);

    long countByLectureId(@Param("lectureId") UUID lectureId);
}
