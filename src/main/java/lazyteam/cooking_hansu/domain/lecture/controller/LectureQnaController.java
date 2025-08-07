package lazyteam.cooking_hansu.domain.lecture.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.LectureQnaCreateDto;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.LectureQnaListDto;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.LectureQnaUpdateDto;
import lazyteam.cooking_hansu.domain.lecture.service.LectureQnaService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lecture/qna")
public class LectureQnaController {

    private final LectureQnaService lectureQnaService;

//    Q&A 등록, Q&A 목록 조회, Q&A 상세조회, Q&A 삭제, Q&A 답변 등록, Q&A 답변 수정, Q&A 답변 삭제

//    Q&A 등록
    @PostMapping("/{lectureId}/create")
    public ResponseEntity<?> createQna(@PathVariable UUID lectureId, @Valid @RequestBody LectureQnaCreateDto lectureQnaCreateDto) {
        UUID qna = lectureQnaService.createQna(lectureId, lectureQnaCreateDto);
        return new ResponseEntity<>(ResponseDto.ok(qna, HttpStatus.CREATED), HttpStatus.CREATED);
    }

//    Q&A 목록 조회
    @GetMapping("/{lectureId}/list")
    public ResponseEntity<?> getQnaList(@PathVariable UUID lectureId) {
        List<LectureQnaListDto> qnaList = lectureQnaService.getQnaList(lectureId);
        return new ResponseEntity<>(ResponseDto.ok(qnaList, HttpStatus.OK), HttpStatus.OK);
    }

//    Q&A 수정
    @PutMapping("/{qnaId}/update")
    public ResponseEntity<?> updateQna(@PathVariable UUID qnaId, @Valid @RequestBody LectureQnaUpdateDto lectureQnaUpdateDto) {
        lectureQnaService.updateQna(qnaId, lectureQnaUpdateDto);
        return new ResponseEntity<>(ResponseDto.ok("Q&A가 수정되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

//    Q&A 삭제
    @DeleteMapping("/{qnaId}/delete")
    public ResponseEntity<?> deleteQna(@PathVariable UUID qnaId) {
        lectureQnaService.deleteQna(qnaId);
        return new ResponseEntity<>(ResponseDto.ok("Q&A가 삭제되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }
}
