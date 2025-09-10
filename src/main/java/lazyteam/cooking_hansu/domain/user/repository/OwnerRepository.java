package lazyteam.cooking_hansu.domain.user.repository;

import lazyteam.cooking_hansu.domain.user.entity.business.Owner;
import lazyteam.cooking_hansu.domain.common.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, UUID> {
    Long countAllByApprovalStatus(ApprovalStatus approvalStatus);

    Page<Owner> findAllByApprovalStatus(Pageable pageable, ApprovalStatus approvalStatus);
}
