package lazyteam.cooking_hansu.domain.report.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.report.dto.ReportDetailDto;
import lazyteam.cooking_hansu.domain.report.service.ReportService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/report")
public class AdminReportController {

    private final ReportService reportService;

//    신고 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> getReportList(@Valid @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReportDetailDto> dtos = reportService.findAll(pageable);
        return new ResponseEntity<>(ResponseDto.ok(dtos, HttpStatus.OK), HttpStatus.OK);
    }

//    신고 승인
    @PatchMapping("/approve/{id}")
    public ResponseEntity<?> approveReport(@PathVariable UUID id) {
        reportService.approveReport(id);
        return new ResponseEntity<>(ResponseDto.ok("신고가 처리되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

//    신고 거절
    @PatchMapping("/reject/{id}")
    public ResponseEntity<?> rejectReport(@PathVariable UUID id, @Valid @RequestBody RejectRequestDto rejectRequestDto) {
        reportService.rejectReport(id, rejectRequestDto);
        return new ResponseEntity<>(ResponseDto.ok("신고가 거절되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }
}
