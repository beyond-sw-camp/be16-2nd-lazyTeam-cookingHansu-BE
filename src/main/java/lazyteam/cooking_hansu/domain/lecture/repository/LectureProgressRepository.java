package lazyteam.cooking_hansu.domain.lecture.repository;

import lazyteam.cooking_hansu.domain.lecture.entity.LectureProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LectureProgressRepository extends JpaRepository<LectureProgress, UUID> {

    // 유저 + 영상 기준 진행도 조회
    Optional<LectureProgress> findByUserIdAndLectureVideoId(UUID userId, UUID lectureVideoId);

    // 특정 강의 내에서 유저가 완료한 영상 수
    Long countByUserIdAndLectureVideo_LectureIdAndCompletedTrue(UUID userId, UUID lectureId);
}