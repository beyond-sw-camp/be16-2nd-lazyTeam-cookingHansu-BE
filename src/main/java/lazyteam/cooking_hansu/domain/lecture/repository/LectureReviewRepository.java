package lazyteam.cooking_hansu.domain.lecture.repository;

import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LectureReviewRepository extends JpaRepository<LectureReview, UUID> {
}
