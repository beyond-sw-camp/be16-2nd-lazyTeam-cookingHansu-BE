package lazyteam.cooking_hansu.domain.report.service;

import lazyteam.cooking_hansu.domain.common.dto.Status;
import lazyteam.cooking_hansu.domain.report.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.report.dto.ReportCreateDto;
import lazyteam.cooking_hansu.domain.report.dto.ReportDetailDto;
import lazyteam.cooking_hansu.domain.report.entity.Report;
import lazyteam.cooking_hansu.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public void createReport(ReportCreateDto reportCreateDto){
//        TODO: 사용자 정보가 맞는지 확인하는 예외 로직 필요함
        Optional<Report> report = reportRepository.findById(reportCreateDto.getReporterId());
        if (report.isPresent() && report.get().getStatus() == Status.PENDING && report.get().getReportType() == reportCreateDto.getReportType() && report.get().getTargetId() == reportCreateDto.getTargetId()) {
            throw new IllegalArgumentException("이미 신고가 접수된 상태입니다. 동일한 신고는 중복 접수가 불가능합니다.");
        }

        reportRepository.save(reportCreateDto.toEntity(reportCreateDto.getReporterId()));
    }

    public Page<ReportDetailDto> findAll(Pageable pageable){
        Page<Report> reportLists = reportRepository.findAllByStatus(pageable, Status.PENDING);
//        TODO:나중에 실제 사용자의 ID로 변경 필요
        return reportLists.map(report -> ReportDetailDto.fromEntity(report, report.getReporterId()));
    }

    public void approveReport(Long id) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new NoSuchElementException("신고 승인할 신고가 없습니다."));
        report.updateStatus(Status.APPROVED, null); // 승인 상태로 업데이트, 거절 사유는 null
    }

    public void rejectReport(Long id, RejectRequestDto rejectRequestDto) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new NoSuchElementException("신고 반려할 신고가 없습니다."));
        report.updateStatus(Status.REJECTED, rejectRequestDto.getReason());
    }
}
