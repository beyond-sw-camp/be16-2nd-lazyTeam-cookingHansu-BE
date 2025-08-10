package lazyteam.cooking_hansu.domain.lecture.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.lecture.dto.review.ReviewCreateDto;
import lazyteam.cooking_hansu.domain.lecture.dto.review.ReviewResDto;
import lazyteam.cooking_hansu.domain.lecture.service.LectureReviewService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class LectureReviewController {

    private final LectureReviewService lectureReviewService;

//    리뷰 등록
    @PostMapping("/post")
    public ResponseEntity<?> create(@Valid @RequestBody ReviewCreateDto reviewCreateDto) {
        lectureReviewService.create(reviewCreateDto);
        return new ResponseEntity<>(ResponseDto.ok("리뷰가 등록되었습니다.", HttpStatus.CREATED),HttpStatus.CREATED);
    }

//    리뷰 조회(페이지네이션)
    @GetMapping("/list/{lectureId}")
    public ResponseEntity<?> reviewFind(@PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                        @PathVariable UUID lectureId) {

        return new ResponseEntity<>(ResponseDto.ok(lectureReviewService.reviewFind(lectureId, pageable),HttpStatus.OK),HttpStatus.OK);
    }


//    리뷰 수정


//    리뷰 삭제

}
