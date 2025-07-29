package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional


public class LectureReviewService {

    private final LectureReviewRepository lectureReviewRepository;


    @Autowired
    public LectureReviewService(LectureReviewRepository lectureReviewRepository) {
        this.lectureReviewRepository = lectureReviewRepository;
    }
}
