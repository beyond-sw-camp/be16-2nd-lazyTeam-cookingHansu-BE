package lazyteam.cooking_hansu.domain.comment.repository;

import lazyteam.cooking_hansu.domain.comment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
}
