package lazyteam.cooking_hansu.domain.lecture.service;

import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.LectureQnaCreateDto;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureQna;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureQnaRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LectureQnaService {

    private final LectureQnaRepository lectureQnaRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    // Q&A 등록
    public void createQna(UUID lectureId, LectureQnaCreateDto lectureQnaCreateDto) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다. lectureId: " + lectureId));
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + userId));

        LectureQna qna = LectureQna.builder()
                .lecture(lecture)
                .questionUser(user)
                .questionText(lectureQnaCreateDto.getQuestionText())
                .build();

        lectureQnaRepository.save(qna);
    }

}
