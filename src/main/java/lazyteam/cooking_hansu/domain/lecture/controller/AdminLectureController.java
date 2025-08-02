package lazyteam.cooking_hansu.domain.lecture.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.lecture.service.LectureService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/lecture")
public class AdminLectureController {
    private final LectureService lectureService;

    @PatchMapping("/approve/{lectureId}")
    public ResponseEntity<?> approveLecture(@PathVariable UUID lectureId){
        lectureService.approveLecture(lectureId);
        return new ResponseEntity<>(ResponseDto.ok("강의가 승인되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

    @PatchMapping("/reject/{lectureId}")
    public ResponseEntity<?> rejectLecture(@Valid @RequestBody RejectRequestDto rejectRequestDto, @PathVariable UUID lectureId){
        lectureService.rejectLecture(lectureId, rejectRequestDto);
        return new ResponseEntity<>(ResponseDto.ok("강의가 거절되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }
}
