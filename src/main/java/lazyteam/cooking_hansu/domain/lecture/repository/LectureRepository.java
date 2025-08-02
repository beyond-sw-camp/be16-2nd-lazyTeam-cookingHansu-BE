package lazyteam.cooking_hansu.domain.lecture.repository;

import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface LectureRepository extends JpaRepository<Lecture, UUID> {

    List<Lecture> findAllByApprovalStatus(ApprovalStatus approvalStatus);

    Long countAllByApprovalStatus(ApprovalStatus approvalStatus);
}
