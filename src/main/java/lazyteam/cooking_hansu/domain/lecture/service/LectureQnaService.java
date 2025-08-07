package lazyteam.cooking_hansu.domain.lecture.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.*;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureQna;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureQnaRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LectureQnaService {

    private final LectureQnaRepository lectureQnaRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    // Q&A 등록
    public UUID createQna(UUID lectureId, LectureQnaCreateDto lectureQnaCreateDto) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강의입니다. lectureId: " + lectureId));
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다. userId: " + userId));

        if(lectureQnaCreateDto.getParentId() !=null){
            LectureQna parentQna = lectureQnaRepository.findById(lectureQnaCreateDto.getParentId()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 부모 Q&A입니다. parentId: " + lectureQnaCreateDto.getParentId()));
            LectureQna childQna = LectureQna.builder()
                    .lecture(lecture)
                    .parent(parentQna)
                    .user(user)
                    .content(lectureQnaCreateDto.getContent())
                    .status(QnaStatus.PENDING)
                    .build();

            parentQna.setChild(childQna);
            parentQna.updateStatus(QnaStatus.ANSWERED);
            return childQna.getId();
        }

        LectureQna qna = LectureQna.builder()
                .lecture(lecture)
                .user(user)
                .content(lectureQnaCreateDto.getContent())
                .build();

        lectureQnaRepository.save(qna);
        return qna.getId();
    }

    // Q&A 목록 조회
    public List<LectureQnaListDto> getQnaList(UUID lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강의입니다. lectureId: " + lectureId));
        List<LectureQna> qnaList = lectureQnaRepository.findAllByLecture(lecture);

        return qnaList.stream()
                .filter(qna -> qna.getParent() == null)
                .map(qna -> LectureQnaListDto.builder()
                        .id(qna.getId())
                        .content(qna.getContent())
                        .status(qna.getStatus())
                        .createdAt(qna.getCreatedAt())
                        .updatedAt(qna.getUpdatedAt())
                        .userName(qna.getUser().getName())
                        .answers(qna.getChild() != null
                                ? List.of(LectureQnaChildDto.builder()
                                .id(qna.getChild().getId())
                                .parentId(qna.getId())
                                .content(qna.getChild().getContent())
                                .createdAt(qna.getChild().getCreatedAt())
                                .updatedAt(qna.getChild().getUpdatedAt())
                                .userName(qna.getChild().getUser().getName())
                                .build())
                                : List.of())
                        .build()
                ).collect(Collectors.toList());
    }

    // Q&A 수정
    public void updateQna(UUID qnaId, LectureQnaUpdateDto lectureQnaUpdateDto){
        LectureQna lectureQna = lectureQnaRepository.findById(qnaId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Q&A입니다. qnaId: " + qnaId));
        lectureQna.updateQnAText(lectureQnaUpdateDto);
    }

    // Q&A 삭제
    public void deleteQna(UUID qnaId) {
        LectureQna lectureQna = lectureQnaRepository.findById(qnaId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Q&A입니다. qnaId: " + qnaId));
        lectureQnaRepository.delete(lectureQna);
    }
}
