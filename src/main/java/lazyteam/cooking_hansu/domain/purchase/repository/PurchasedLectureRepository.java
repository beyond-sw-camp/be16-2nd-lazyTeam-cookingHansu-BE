package lazyteam.cooking_hansu.domain.purchase.repository;


import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PurchasedLectureRepository extends JpaRepository<PurchasedLecture, UUID> {
    Page<PurchasedLecture> findAllByUser(User user, Pageable pageable);
    int countByLecture(Lecture lecture);

}
