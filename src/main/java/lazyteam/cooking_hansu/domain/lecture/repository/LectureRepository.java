package lazyteam.cooking_hansu.domain.lecture.repository;

import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    List<Lecture> findAllByApprovalStatus(ApprovalStatus approvalStatus);

    Long countAllByApprovalStatus(ApprovalStatus approvalStatus);
}
