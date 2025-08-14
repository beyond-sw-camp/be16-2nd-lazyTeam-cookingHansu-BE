package lazyteam.cooking_hansu.domain.recipe.repository;

import lazyteam.cooking_hansu.domain.recipe.entity.PostSequenceDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostSequenceDescriptionRepository extends JpaRepository<PostSequenceDescription, UUID> {
    
    /**
     * 특정 게시글의 모든 조리순서 설명 조회 (순서대로 정렬)
     */
    @Query("SELECT psd FROM PostSequenceDescription psd " +
           "JOIN FETCH psd.recipeStep rs " +
           "WHERE psd.post.id = :postId " +
           "ORDER BY rs.stepSequence ASC")
    List<PostSequenceDescription> findByPostIdOrderByStepSequence(@Param("postId") UUID postId);
    
    /**
     * 특정 게시글의 조리순서 설명 개수 조회
     */
    long countByPostId(UUID postId);
    
    /**
     * 특정 레시피 단계와 연결된 게시글들의 설명 조회
     */
    List<PostSequenceDescription> findByRecipeStepId(UUID stepId);
    
    /**
     * 특정 게시글과 레시피 단계의 설명이 존재하는지 확인
     */
    boolean existsByPostIdAndRecipeStepId(UUID postId, UUID stepId);
    
    /**
     * 특정 게시글의 모든 조리순서 설명 삭제
     */
    void deleteByPostId(UUID postId);
    
    /**
     * 특정 레시피의 모든 단계와 연결된 게시글 설명들 조회
     */
    @Query("SELECT psd FROM PostSequenceDescription psd " +
           "JOIN FETCH psd.recipeStep rs " +
           "JOIN FETCH rs.recipe r " +
           "WHERE r.id = :recipeId " +
           "ORDER BY rs.stepSequence ASC")
    List<PostSequenceDescription> findByRecipeIdOrderByStepSequence(@Param("recipeId") UUID recipeId);
    
    /**
     * 게시글과 연관된 레시피 정보 조회 (첫 번째 단계를 통해)
     */
    @Query("SELECT DISTINCT rs.recipe FROM PostSequenceDescription psd " +
           "JOIN psd.recipeStep rs " +
           "WHERE psd.post.id = :postId")
    List<Object> findRecipeByPostId(@Param("postId") UUID postId);
}
