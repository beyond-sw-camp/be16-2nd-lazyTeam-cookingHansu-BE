package lazyteam.cooking_hansu.domain.user.repository;

import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChefRepository extends JpaRepository<Chef, UUID> {
    Long countAllByApprovalStatus(ApprovalStatus approvalStatus);
}
