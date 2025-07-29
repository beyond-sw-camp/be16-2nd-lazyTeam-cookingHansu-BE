package lazyteam.cooking_hansu.domain.lecture.service;

import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureQnaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class LectureQnaService {

    private final LectureQnaRepository lectureQnaRepository;

    @Autowired
    public LectureQnaService(LectureQnaRepository lectureQnaRepository) {
        this.lectureQnaRepository = lectureQnaRepository;
    }




}
