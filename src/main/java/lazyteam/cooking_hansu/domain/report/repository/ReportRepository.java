package lazyteam.cooking_hansu.domain.report.repository;

import lazyteam.cooking_hansu.domain.common.dto.Status;
import lazyteam.cooking_hansu.domain.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findAllByStatus(Pageable pageable, Status status);

    List<Report> findAllByStatus(Status status);
}
