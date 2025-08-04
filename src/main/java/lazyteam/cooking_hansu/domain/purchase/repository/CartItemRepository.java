package lazyteam.cooking_hansu.domain.purchase.repository;

import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.purchase.entity.CartItem;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    boolean existsByUserAndLecture(User user, Lecture lecture);

    List<CartItem> findAllByUser(User user);

    // 일괄 삭제용
    Optional<CartItem> findByUserAndLecture(User user, Lecture lecture);

    // 단건 삭제용
    void deleteAllByUser(User user);

    // 장바구니에서 유저의 강의 ID 조회하여 결제 완료시 삭제하기 위한 메서드
    List<CartItem> findAllByUserAndLectureIdIn(User user, List<UUID> lectureIds);

}
