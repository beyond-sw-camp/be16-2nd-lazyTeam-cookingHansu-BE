package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional

//Lecture, CopyOfLectureStep, LectureIngredientsList, LectureVideo 엔티티 서비스
public class LectureService {

    private final LectureRepository lectureRepository;

    @Autowired
    public LectureService(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }
}
