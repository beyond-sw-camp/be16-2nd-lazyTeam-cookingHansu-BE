package lazyteam.cooking_hansu.domain.lecture.repository;


import org.springframework.data.domain.Pageable;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LectureReviewRepository extends JpaRepository<LectureReview, UUID> {
    Page<LectureReview> findAllByLectureId(UUID lectureId, Pageable pageable);
    List<LectureReview> findAllByLectureId(UUID lectureId);
    Optional<LectureReview> findByLectureIdAndWriterId(UUID lectureId, UUID userId);

    LectureReview findByWriterId(UUID userId);

}
