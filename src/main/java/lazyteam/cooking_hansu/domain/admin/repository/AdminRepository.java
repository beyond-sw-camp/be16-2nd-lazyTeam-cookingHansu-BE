package lazyteam.cooking_hansu.domain.admin.repository;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
    // 관리자 이메일로 관리자 정보 조회
    Optional<Admin> findByEmail(String email);
}
