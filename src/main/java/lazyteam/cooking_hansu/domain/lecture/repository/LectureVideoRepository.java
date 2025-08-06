package lazyteam.cooking_hansu.domain.lecture.repository;

import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;


@Repository
public interface LectureVideoRepository extends JpaRepository<LectureVideo, UUID> {

    // 특정 강의의 총 duration을 계산하는 메서드
    @Query("SELECT COALESCE(SUM(lv.duration), 0) FROM LectureVideo lv WHERE lv.lecture.id = :lectureId")
    Integer getTotalDurationByLectureId(@Param("lectureId") UUID lectureId);

    void deleteByLecture(Lecture lecture);

    List<LectureVideo> findByLecture(Lecture lecture);

}
