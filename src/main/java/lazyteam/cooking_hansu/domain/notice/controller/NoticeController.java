package lazyteam.cooking_hansu.domain.notice.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeCreateDto;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeDetailDto;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeUpdateDto;
import lazyteam.cooking_hansu.domain.notice.service.NoticeService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeService noticeService;

    //     공지사항 전체 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> noticeLists(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new ResponseEntity<>(ResponseDto.ok(noticeService.findAll(pageable), HttpStatus.OK), HttpStatus.OK);
    }

    //     공지사항 상세조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        return new ResponseEntity<>(ResponseDto.ok(noticeService.findById(id), HttpStatus.OK), HttpStatus.OK);
    }

}
