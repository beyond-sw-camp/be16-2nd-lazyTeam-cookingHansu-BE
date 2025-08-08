package lazyteam.cooking_hansu.domain.lecture.controller;

import lazyteam.cooking_hansu.domain.lecture.service.LectureReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class LectureReviewController {

    private final LectureReviewService lectureReviewService;

//    리뷰 등록


//    리뷰 조회(페이지네이션)


//    리뷰 수정


//    리뷰 삭제

}
