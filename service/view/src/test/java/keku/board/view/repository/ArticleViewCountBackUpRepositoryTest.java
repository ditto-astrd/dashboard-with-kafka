package keku.board.view.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Attr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import keku.board.view.entity.ArticleViewCount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ArticleViewCountBackUpRepositoryTest {

  @Autowired
  ArticleViewCountBackUpRepository articleViewCountBackUpRepository;
  @PersistenceContext
  EntityManager entityManager;

  @Test
  @Transactional
  void updateViewCountTest() {
    // given
    articleViewCountBackUpRepository.save(
        ArticleViewCount.init(1L, 0L)
    );

    // INSERT 쿼리 즉시 실행
    entityManager.flush();
    // 1차 캐시 비우기 (DB와 새로 동기화)
    entityManager.clear();

    // 요청이 한 번에 많이 들어올 때
    // 100, 300, 200.. 이런 경우에 대한 방어 코드

    // when
    int result1 = articleViewCountBackUpRepository.updateViewCount(1L, 100L);
    int result2 = articleViewCountBackUpRepository.updateViewCount(1L, 300L);
    int result3 = articleViewCountBackUpRepository.updateViewCount(1L, 200L);

    // then
    assertThat(result1).isEqualTo(1);
    assertThat(result2).isEqualTo(1);
    assertThat(result3).isEqualTo(0);

    ArticleViewCount articleViewCount = articleViewCountBackUpRepository.findById(1L).get();
    assertThat(articleViewCount.getViewCount()).isEqualTo(300L);
  }

}
