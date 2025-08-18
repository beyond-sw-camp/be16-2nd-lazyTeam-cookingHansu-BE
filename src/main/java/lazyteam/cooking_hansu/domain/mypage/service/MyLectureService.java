package lazyteam.cooking_hansu.domain.mypage.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureReviewRepository;
import lazyteam.cooking_hansu.domain.mypage.dto.MyLectureListDto;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lazyteam.cooking_hansu.domain.purchase.repository.PurchasedLectureRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyLectureService {

    private final PurchasedLectureRepository purchasedLectureRepository;
    private final LectureReviewRepository lectureReviewRepository;
    private final UserRepository userRepository;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    @Transactional(readOnly = true)
    public Page<MyLectureListDto> myLectures(Pageable pageable) {
        UUID userId = UUID.fromString(testUserIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        // 해당 유저가 구매한 강의 목록
        Page<PurchasedLecture> purchases = purchasedLectureRepository.findAllByUser(user, pageable);

        return purchases.map(purchase -> {
                    Lecture lecture = purchase.getLecture();

                    // 평균 평점 계산
                    Page<LectureReview> reviewsPages = lectureReviewRepository.findAllByLectureId(lecture.getId(),  Pageable.unpaged());
                    // 리뷰조회 시 page 타입으로 리턴이 필요해서 변환처리
                    List<LectureReview> reviews = reviewsPages.getContent();
            double avgRating = reviews.isEmpty() ? 0.0 :
                            reviews.stream().mapToInt(LectureReview::getRating).average().orElse(0.0);

                    // 수강생 수 계산
                    int studentCount = purchasedLectureRepository.countByLecture(lecture);

                    return MyLectureListDto.builder()
                            .id(lecture.getId())
                            .category(lecture.getCategory().toString())
                            .title(lecture.getTitle())
                            .description(lecture.getDescription())
                            .averageRating(avgRating)
                            .studentCount(studentCount)
                            .thumbnailUrl(lecture.getThumbUrl())
                            .build();
                });
    }
}
