package lazyteam.cooking_hansu.domain.user.repository;

import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {
}
