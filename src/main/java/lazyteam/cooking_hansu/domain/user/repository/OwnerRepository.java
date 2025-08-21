package lazyteam.cooking_hansu.domain.user.repository;

import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.user.entity.business.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, UUID> {
    Long countAllByApprovalStatus(ApprovalStatus approvalStatus);

    Page<Owner> findAllByApprovalStatus(Pageable pageable, ApprovalStatus approvalStatus);
}
