package lazyteam.cooking_hansu.domain.lecture.repository;

import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface LectureRepository extends JpaRepository<Lecture, UUID> {

    Page<Lecture> findAllByApprovalStatus(Pageable pageable, ApprovalStatus approvalStatus);

    Long countAllByApprovalStatus(ApprovalStatus approvalStatus);
}
