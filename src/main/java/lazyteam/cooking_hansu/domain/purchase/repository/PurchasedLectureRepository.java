package lazyteam.cooking_hansu.domain.purchase.repository;

import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PurchasedLectureRepository extends JpaRepository<PurchasedLecture, UUID> {
}
