package lazyteam.cooking_hansu.domain.notice.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeCreateDto;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeResDto;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeUpdateDto;
import lazyteam.cooking_hansu.domain.notice.service.NoticeService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
 @PreAuthorize("hasRole('ADMIN')") // 테스트 위해 주석처리
@RequestMapping("/admin/notice")
public class AdminNoticeController {

    private final NoticeService noticeService;

    //     공지사항 등록
    @PostMapping("/create")
    public ResponseEntity<?> createNotice(@ModelAttribute @Valid NoticeCreateDto noticeCreateDto) {
        NoticeResDto notice = noticeService.createNotice(noticeCreateDto);
        return new ResponseEntity<>(ResponseDto.ok(notice, HttpStatus.CREATED), HttpStatus.CREATED);
    }


    //     공지사항 수정
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateNotice(@ModelAttribute @Valid NoticeUpdateDto noticeUpdateDto, @PathVariable UUID id) {
        NoticeResDto notice = noticeService.updateNotice(id, noticeUpdateDto);
        return new ResponseEntity<>(ResponseDto.ok(notice, HttpStatus.OK), HttpStatus.OK);
    }

    //     공지사항 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable UUID id) {
        noticeService.deleteNotice(id);
        return new ResponseEntity<>(ResponseDto.ok("공지사항이 삭제되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }
}
