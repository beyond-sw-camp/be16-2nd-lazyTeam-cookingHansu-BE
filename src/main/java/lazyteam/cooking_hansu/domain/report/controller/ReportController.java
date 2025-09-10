package lazyteam.cooking_hansu.domain.report.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.report.dto.ReportCreateDto;
import lazyteam.cooking_hansu.domain.report.service.ReportService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasAnyRole('CHEF', 'OWNER','GENERAL')")
    @PostMapping("/create")
    public ResponseEntity<?> createReport(@Valid @RequestBody ReportCreateDto reportCreateDto) {
        reportService.createReport(reportCreateDto);
        return new ResponseEntity<>(ResponseDto.ok("신고가 접수되었습니다.", HttpStatus.CREATED), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('CHEF', 'OWNER','GENERAL')")
    @GetMapping("/check/{targetId}")
    public ResponseEntity<?> checkReport(@Valid @PathVariable UUID targetId) {
        return new ResponseEntity<>(ResponseDto.ok(reportService.checkReport(targetId), HttpStatus.OK), HttpStatus.OK);
    }

}
