package lazyteam.cooking_hansu.domain.report.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.common.dto.Status;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.report.dto.ReportCreateDto;
import lazyteam.cooking_hansu.domain.report.dto.ReportDetailDto;
import lazyteam.cooking_hansu.domain.report.entity.Report;
import lazyteam.cooking_hansu.domain.report.repository.ReportRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public void createReport(ReportCreateDto reportCreateDto){
        // TODO: 실제 인증된 사용자 정보로 변경 필요
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("신고를 생성할 사용자를 찾을 수 없습니다."));
        List<Report> reportList = reportRepository.findAllByStatus(Status.PENDING);
//        이미 신고가 되어있고 같은사람이면 중복신고못하게 막기
        if(reportList.stream().anyMatch(r -> r.getUser().getId().equals(user.getId()) && r.getTargetId().equals(reportCreateDto.getTargetId()))) {
            throw new IllegalArgumentException("같은 사용자에 대한 중복 신고는 허용되지 않습니다. 신고가 처리된 이후에 다시 시도해주세요.");
        }

        reportRepository.save(reportCreateDto.toEntity(user));
    }


    public Page<ReportDetailDto> findAll(Pageable pageable){
        Page<Report> reportLists = reportRepository.findAllByStatus(pageable, Status.PENDING);
        return reportLists.map(ReportDetailDto::fromEntity);
    }

    public void approveReport(UUID id) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("승인할 신고가 없습니다."));
        if (report.getStatus() == Status.APPROVED) {
            throw new IllegalArgumentException("이미 처리된 신고입니다. 다시 확인해주세요.");
        }
        report.updateStatus(Status.APPROVED, null); // 승인 상태로 업데이트, 거절 사유는 null

        //        TODO:나중에 승인 알림 기능 추가 필요
    }

    public void rejectReport(UUID id, RejectRequestDto rejectRequestDto) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("반려할 신고가 없습니다."));
        if (report.getStatus() == Status.REJECTED) {
            throw new IllegalArgumentException("이미 처리된 신고입니다. 다시 확인해주세요.");
        }
        report.updateStatus(Status.REJECTED, rejectRequestDto.getReason());

        //        TODO:나중에 승인 알림 기능 추가 필요
    }
}
