package lazyteam.cooking_hansu.domain.lecture.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.*;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureQna;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureQnaRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.domain.notification.service.NotificationService;
import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lazyteam.cooking_hansu.global.auth.dto.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LectureQnaService {

    private static final Logger log = LoggerFactory.getLogger(LectureQnaService.class);
    private final LectureQnaRepository lectureQnaRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Q&A 등록
    public UUID createQna(UUID lectureId, LectureQnaCreateDto lectureQnaCreateDto) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강의입니다. lectureId: " + lectureId));
        UUID userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        log.info(lectureQnaCreateDto.toString());
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

//        강의 qna 수 갱신 로직
            lecture.setQnaCount(
                    (lecture.getQnaCount() == null ? 0 : lecture.getQnaCount()) + 1
            );

            // QNA 답변 알림 발송 (질문자에게)
            String notificationContent = String.format("질문에 답변이 등록되었습니다: \"%s\"", 
                    parentQna.getContent().length() > 30 
                            ? parentQna.getContent().substring(0, 30) + "..." 
                            : parentQna.getContent());
            
            SseMessageDto sseMessageDto = SseMessageDto.builder()
                    .recipientId(parentQna.getUser().getId())
                    .targetType(TargetType.QNACOMMENT)
                    .targetId(lecture.getId())
                    .content(notificationContent)
                    .build();
            
            notificationService.createAndDispatch(sseMessageDto);


            return childQna.getId();
        }

        LectureQna qna = LectureQna.builder()
                .lecture(lecture)
                .user(user)
                .content(lectureQnaCreateDto.getContent())
                .build();

//        강의 qna 수 갱신 로직
        lecture.setQnaCount(
                (lecture.getQnaCount() == null ? 0 : lecture.getQnaCount()) + 1
        );

        lectureQnaRepository.save(qna);

        // 질문 등록 알림 발송 (강의 등록자에게)
        // 질문자가 강의 등록자가 아닌 경우에만 알림 발송
        if (!user.getId().equals(lecture.getSubmittedBy().getId())) {
            String notificationContent = String.format("강의에 새로운 질문이 등록되었습니다: \"%s\"", 
                    qna.getContent().length() > 30 
                            ? qna.getContent().substring(0, 30) + "..." 
                            : qna.getContent());
            
            SseMessageDto sseMessageDto = SseMessageDto.builder()
                    .recipientId(lecture.getSubmittedBy().getId())
                    .targetType(TargetType.QNACOMMENT)
                    .targetId(lecture.getId())
                    .content(notificationContent)
                    .build();
            
            notificationService.createAndDispatch(sseMessageDto);
        }

        return qna.getId();
    }

    // Q&A 목록 조회
    public List<LectureQnaListDto> getQnaList(UUID lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강의입니다. lectureId: " + lectureId));
        List<LectureQna> qnaList = lectureQnaRepository.findAllByLecture(lecture).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Q&A 목록입니다. lectureId: " + lectureId));

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

//        강의 qna 수 갱신 로직
        Lecture lecture = lectureQna.getLecture();
        lecture.setQnaCount(
                Math.max(0, (lecture.getQnaCount() == null ? 0 : lecture.getQnaCount()) - 1)
        );

        lectureQnaRepository.delete(lectureQna);
    }
}
