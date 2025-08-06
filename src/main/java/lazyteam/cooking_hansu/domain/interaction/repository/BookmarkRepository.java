package lazyteam.cooking_hansu.domain.interaction.repository;

import lazyteam.cooking_hansu.domain.interaction.entity.Bookmark;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
    List<Bookmark> findAllByUser(User user);
}
