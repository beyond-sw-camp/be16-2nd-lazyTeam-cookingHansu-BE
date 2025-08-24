package lazyteam.cooking_hansu.domain.post.repository;

import lazyteam.cooking_hansu.domain.post.entity.Ingredients;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IngredientsRepository extends JpaRepository<Ingredients, UUID> {

//    Post 조회시 재료 목록 필요
    List<Ingredients> findByPost(Post post);

//    Post 수정/삭제시 재료관리
    void deleteByPost(Post post);
}
