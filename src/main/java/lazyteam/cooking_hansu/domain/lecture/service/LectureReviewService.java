package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor

public class LectureReviewService {

    private final LectureReviewRepository lectureReviewRepository;






}
