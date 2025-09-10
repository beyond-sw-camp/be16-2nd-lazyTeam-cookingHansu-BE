package lazyteam.cooking_hansu.domain.lecture.repository;

import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LectureStepRepository extends JpaRepository<LectureStep, UUID> {
    void deleteByLecture(Lecture lecture);
}
