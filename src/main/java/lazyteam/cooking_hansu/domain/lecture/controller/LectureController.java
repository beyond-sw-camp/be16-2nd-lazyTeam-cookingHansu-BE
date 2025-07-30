package lazyteam.cooking_hansu.domain.lecture.controller;


import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.lecture.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("lecture")
@RequiredArgsConstructor
//Lecture, CopyOfLectureStep, LectureIngredientsList, LectureVideo 엔티티 서비스
public class LectureController {

    private final LectureService lectureService;

//    강의 등록, 강의 목록 조회, 강의 상세조회, 강의삭제?
    @RequestMapping("/post")
    public ResponseEntity<?> create(@Valid @RequestBody ) {

        return null;
    }



}
