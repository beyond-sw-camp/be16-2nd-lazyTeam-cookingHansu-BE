package lazyteam.cooking_hansu.domain.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CopyOfLectureStepRepository extends JpaRepository<CopyOfLectureStepRepository, Long> {
}
