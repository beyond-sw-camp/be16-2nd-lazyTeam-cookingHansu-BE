package lazyteam.cooking_hansu.domain.post.repository;

import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeStepRepository extends JpaRepository<RecipeStep, UUID> {

    // Post 조회시 조리순서 목록
    List<RecipeStep> findByPostOrderByStepSequence(Post post);

    // Post 수정/삭제 시 조리순서 관리
    void deleteByPost(Post post);
}
