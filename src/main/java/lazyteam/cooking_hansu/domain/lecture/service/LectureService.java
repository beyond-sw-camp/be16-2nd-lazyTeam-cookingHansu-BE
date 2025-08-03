package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.lecture.dto.WaitingLectureDto;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureVideo;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureVideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional

//Lecture, CopyOfLectureStep, LectureIngredientsList, LectureVideo 엔티티 서비스
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureVideoRepository lectureVideoRepository;

    @Autowired
    public LectureService(LectureRepository lectureRepository, LectureVideoRepository lectureVideoRepository) {
        this.lectureRepository = lectureRepository;
        this.lectureVideoRepository = lectureVideoRepository;
    }


//    강의 목록 조회(승인 안된 강의 목록 조회)
    public Page<WaitingLectureDto> getWaitingLectureList(Pageable pageable){
        Page<Lecture> lectures = lectureRepository.findAllByApprovalStatus(pageable, ApprovalStatus.PENDING);
        return lectures.map(lecture -> WaitingLectureDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .imageUrl(lecture.getThumbUrl())
                .category(lecture.getCategory())
                .instructorName(lecture.getSubmittedBy().getName())
                .status(lecture.getApprovalStatus())
                .price(lecture.getPrice())
                .duration(lectureVideoRepository.getTotalDurationByLectureId(lecture.getId()))
                .build());
    }

//    강의 승인
    public void approveLecture(UUID lectureId){
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다. lectureId: " + lectureId));
        if(lecture.getApprovalStatus() != null && lecture.getApprovalStatus().equals(ApprovalStatus.APPROVED)) {
            throw new IllegalArgumentException("이미 승인된 강의입니다. lectureId: " + lectureId);
        }
        lecture.approve();
    }

//    강의 거절
    public void rejectLecture(UUID lectureId, RejectRequestDto rejectRequestDto) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다. lectureId: " + lectureId));
        if (lecture.getApprovalStatus() != null && lecture.getApprovalStatus().equals(ApprovalStatus.REJECTED)) {
            throw new IllegalArgumentException("이미 거절된 강의입니다. lectureId: " + lectureId);
        }
        lecture.reject(rejectRequestDto.getReason());
    }
}
