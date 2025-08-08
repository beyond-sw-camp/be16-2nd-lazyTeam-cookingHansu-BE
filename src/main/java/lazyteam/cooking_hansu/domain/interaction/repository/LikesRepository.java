package lazyteam.cooking_hansu.domain.interaction.repository;

import lazyteam.cooking_hansu.domain.interaction.entity.Likes;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LikesRepository extends JpaRepository<Likes, UUID> {
    List<Likes> findAllByUser(User user);
}


