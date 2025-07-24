package lazyteam.cooking_hansu.domain.report.controller;
import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.report.dto.ReportCreateDto;
import lazyteam.cooking_hansu.domain.report.service.ReportService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/create")
    public ResponseEntity<?> createReport(@Valid @RequestBody ReportCreateDto reportCreateDto) {
        reportService.createReport(reportCreateDto);
        return new ResponseEntity<>(ResponseDto.ok("신고가 접수되었습니다.", HttpStatus.CREATED), HttpStatus.CREATED);
    }

}
