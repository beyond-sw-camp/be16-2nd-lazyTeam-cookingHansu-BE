package lazyteam.cooking_hansu.domain.lecture.repository;

import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface LectureIngredientsListRepository extends JpaRepository<Lecture, Long> {
    
}
