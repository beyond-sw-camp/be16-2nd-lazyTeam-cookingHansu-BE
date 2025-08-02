package lazyteam.cooking_hansu.domain.purchase.repository;

import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchasedLectureRepository extends JpaRepository<PurchasedLecture, Long> {
    List<PurchasedLecture> findAllByUser(User user);
    int countByLecture(Lecture lecture);

}
