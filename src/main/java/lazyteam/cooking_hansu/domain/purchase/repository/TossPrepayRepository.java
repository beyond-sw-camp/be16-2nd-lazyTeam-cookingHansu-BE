package lazyteam.cooking_hansu.domain.purchase.repository;

import lazyteam.cooking_hansu.domain.purchase.entity.TossPrePay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TossPrepayRepository extends JpaRepository<TossPrePay, Long> {
    Optional<TossPrePay> findByOrderId(String orderId);
}
