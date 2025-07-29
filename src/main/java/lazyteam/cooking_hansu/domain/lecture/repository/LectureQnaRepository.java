package lazyteam.cooking_hansu.domain.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureQnaRepository extends JpaRepository<LectureQnaRepository,Long> {

}
