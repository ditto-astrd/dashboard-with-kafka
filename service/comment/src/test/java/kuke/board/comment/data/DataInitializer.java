package kuke.board.comment.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kuke.board.comment.CommentApplication;
import kuke.board.comment.entity.Comment;
import kuke.board.common.snowflake.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = CommentApplication.class)
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
      Comment prev = null;

      // i가 짝수면 상위 댓글이 없고, 홀수면 이전 댓글을 상위 댓글로 지정
      for (int i = 0; i < BULK_INSERT_SIZE; i++) {
        Comment comment = Comment.create(
            snowflake.nextId(),
            "conent",
            i % 2 == 0 ? null : prev.getCommentId(),
            1L,
            1L
        );

        prev = comment;
        entityManager.persist(comment);
      }
    });
  }
}
