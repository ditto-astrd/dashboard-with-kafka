package keku.board.view.repository;

import keku.board.view.entity.ArticleViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleViewCountBackUpRepository extends JpaRepository<ArticleViewCount, Long> {

  // update 결과가 있으면 1, 없으면 0 반환
  @Query(
      value = "UPDATE article_view_count SET view_count = :viewCount " +
          "WHERE article_id= :articleId AND view_count < :viewCount",
      nativeQuery = true
  )
  @Modifying
  int updateViewCount(
    @Param("articleId") Long articleId,
      @Param("viewCount") Long viewCount
  );

}
