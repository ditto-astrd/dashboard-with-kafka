package kuke.board.article.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import keku.board.article.ArticleApplication;
import keku.board.article.entity.Article;
import kuke.board.common.snowflake.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = ArticleApplication.class)
public class DataInitializer {

  @PersistenceContext
  EntityManager entityManager;

  @Autowired
  TransactionTemplate transactionTemplate;
  Snowflake snowflake = new Snowflake();
  CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

  static final int BULK_INSERT_SIZE = 2000;
  static final int EXECUTE_COUNT = 6000;

  @Test
  void initialize() throws InterruptedException {
    // 10개의 thread pool을 사용해 동시에 실행
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    for (int i = 0; i < EXECUTE_COUNT; i++) {
      executorService.submit(() -> {
        insert();
        latch.countDown();
        System.out.println("latch.getCount() = " + latch.getCount());
      });
    }
    latch.await();
    executorService.shutdown();
  }

  @Test
  void insert() {
    transactionTemplate.executeWithoutResult(status -> {
      for (int i = 0; i < BULK_INSERT_SIZE; i++) {
        Article article = Article.create(
            snowflake.nextId(),
            "title" + 1,
            "content" + 1,
            1L,
            1L
        );
        // Article 객체를 영속성 컨텍스트에 등록하고
        // 트랜잭션 커밋 시점에 DB에 insert 실행
        entityManager.persist(article);
        // save를 하면 새로운 객체는 insert, 이미 있으면 update
        // persist를 하면 insert만 함 (새 객체를 저장)
        // 만약 같은 ID가 있으면 예외 발생
      }
    });
  }
}
