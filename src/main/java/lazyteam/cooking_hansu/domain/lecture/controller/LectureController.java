package lazyteam.cooking_hansu.domain.lecture.controller;


import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.lecture.dto.LectureCreateDto;
import lazyteam.cooking_hansu.domain.lecture.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

//    강의등록

//    메모 : 아마존 config 추가, 강의ID > id로 통일시키기 , yaml 질문 ,findbyemail 구현필ㄹ요한데..

    @PostMapping("/post")
    public ResponseEntity<?> create(@Valid @RequestBody LectureCreateDto lectureCreateDto) {
        Long lectureId = lectureService.create(lectureCreateDto);
        return null;
    }

    @PostMapping("/update")
    public ResponseEntity<?> lectureUpdate(@Valid @RequestBody LectureUpdateDto lectureUpdateDto) {
        lectureService.lectureUpdate();
        return null;
    }



}
