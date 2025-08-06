package lazyteam.cooking_hansu.domain.lecture.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.LectureQnaCreateDto;
import lazyteam.cooking_hansu.domain.lecture.service.LectureQnaService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lecture/qna")
public class LectureQnaController {

    private final LectureQnaService lectureQnaService;

//    Q&A 등록, Q&A 목록 조회, Q&A 상세조회, Q&A 삭제, Q&A 답변 등록, Q&A 답변 수정, Q&A 답변 삭제

     @PostMapping("/create")
    public ResponseEntity<?> createQna(@PathVariable UUID lectureId, @Valid @RequestBody LectureQnaCreateDto lectureQnaCreateDto) {
         lectureQnaService.createQna(lectureId, lectureQnaCreateDto);
         return new ResponseEntity<>(ResponseDto.ok("Q&A가 등록되었습니다.", HttpStatus.CREATED), HttpStatus.CREATED);
     }

}
