package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.dto.review.ReviewCreateDto;
import lazyteam.cooking_hansu.domain.lecture.dto.review.ReviewModifyDto;
import lazyteam.cooking_hansu.domain.lecture.dto.review.ReviewResDto;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureReviewRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor

public class LectureReviewService {

    private final LectureReviewRepository lectureReviewRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;

//    리뷰번호는 데이터조회 빈번하지 않을 것 같아 void로 설정
//    리뷰 작성 : 리뷰 쓸 강의의 ID와 작성자(강의를 구매한 일반 회원)의 ID를 매개변수로 받아 toEntity
    public void create(ReviewCreateDto reviewCreateDto) {

        //        테스트용 유저 세팅
        UUID testUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("테스트 유저가 없습니다."));

        if(reviewCreateDto.getRating()<1 || reviewCreateDto.getRating()>5) {
//            디버깅용
            throw new IllegalArgumentException("평점의 값이 1보다 작거나 5보다 큽니다.");
        }

        Lecture lecture = lectureRepository.findById(reviewCreateDto.getLectureId())
                .orElseThrow(()->new EntityNotFoundException("해당 ID 존재하지 않습니다."));

        if(lectureReviewRepository.findByWriterId(user.getId())!=null) {
            throw new DataIntegrityViolationException("해당 리뷰가 이미 존재합니다.");
        }
        lecture.setReviewCount(lecture.getReviewCount() + 1);
        lectureReviewRepository.save(reviewCreateDto.toEntity(lecture,user));
    }

//    강의 리뷰 조회
    public Page<ReviewResDto> reviewFind(UUID lectureID, Pageable pageable) {
        Page<LectureReview> reviewPageList  = lectureReviewRepository.findAllByLectureId(lectureID, pageable);
        if (reviewPageList.isEmpty()) {
            throw new EntityNotFoundException("해당 강의에 리뷰가 없습니다.");
        }
        return reviewPageList
                .map(r -> ReviewResDto.fromEntity(
                        r.getWriter(),
                        r.getRating(),
                        r.getContent()
                ));

    }

//    강의 리뷰 수정
    public void reviewModify(ReviewModifyDto reviewModifyDto) {
        //        테스트용 유저 세팅
        UUID testUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("테스트 유저가 없습니다."));
        if(reviewModifyDto.getRating()!=null) {
            if(reviewModifyDto.getRating()<1 || reviewModifyDto.getRating()>5) {

                throw new IllegalArgumentException("평점의 값이 1보다 작거나 5보다 큽니다.");
            }
        }


        Lecture lecture = lectureRepository.findById(reviewModifyDto.getLectureId())
                .orElseThrow(()->new EntityNotFoundException("해당 ID 존재하지 않습니다."));

        LectureReview lectureReview = lectureReviewRepository.findByLectureIdAndWriterId(lecture.getId(),user.getId())
                .orElseThrow(()->new EntityNotFoundException("해당 ID 리뷰 존재하지 않습니다."));

        lectureReview.modifyReview(reviewModifyDto);

    }

//    리뷰 삭제
    public void reviewDelete(UUID lectureId) {
        //        테스트용 유저 세팅
        UUID testUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("테스트 유저가 없습니다."));
        LectureReview lectureReview = lectureReviewRepository.findByLectureIdAndWriterId(lectureId,user.getId())
                .orElseThrow(()->new EntityNotFoundException("해당 ID 리뷰 존재하지 않습니다."));
        lectureReviewRepository.delete(lectureReview);

        Lecture lecture = lectureReview.getLecture();
        lecture.setReviewCount(Math.max(0, lecture.getReviewCount() - 1));
    }




}
