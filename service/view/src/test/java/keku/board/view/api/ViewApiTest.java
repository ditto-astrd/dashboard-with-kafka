package keku.board.view.api;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class ViewApiTest {
  RestClient restClient = RestClient.create("http://localhost:9003");

  // 분산락이 걸려있는 코드를 반영했다면  count는 1일 수 밖에 없음
  @Test
  void viewTest() throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(100);
    // 여러 작업이 끝날때까지 기다릴 수 이게해주는 동기화 도구
    CountDownLatch latch = new CountDownLatch(10000);

    for(int i=0; i<10000; i++) {
      // 스레드 풀은 동시에 최대 100개까지만 실행 가능 (newFixedThreadPool)
      executorService.submit(() -> {
        restClient.post()
            .uri("/v1/article-views/articles/{articleId}/users/{userId}", 6L, 1L)
            .retrieve();
        latch.countDown();
      });
    }

    // 10,000개의 요청이 다 끝날때까지 기다림
    // count가 0이어야 끝나므로, latch.countDown()과 한 셋트
    latch.await();

    Long count = restClient.get()
        .uri("/v1/article-views/articles/{articleId}/count", 6L)
        .retrieve()
        .body(Long.class);

    System.out.println("count = " + count);
  }
}

