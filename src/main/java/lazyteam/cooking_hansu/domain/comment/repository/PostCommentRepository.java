package lazyteam.cooking_hansu.domain.comment.repository;

import lazyteam.cooking_hansu.domain.comment.entity.PostComment;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    List<PostComment> findAllByPostAndParentCommentIsNull(Post post);
    Long countByPostAndCommentIsDeletedFalse(Post post);
}
