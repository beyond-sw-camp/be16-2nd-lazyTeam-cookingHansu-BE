package lazyteam.cooking_hansu.domain.notice.repository;

import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, UUID> {
    Page<Notice> findAll(Pageable pageable);
}
