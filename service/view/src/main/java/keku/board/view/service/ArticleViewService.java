package keku.board.view.service;

import java.time.Duration;
import keku.board.view.repository.ArticleViewCountRepository;
import keku.board.view.repository.ArticleViewDistributedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
  private static final int BACK_UP_BATCH_SIZE = 100;
  private static final Duration TTL = Duration.ofMinutes(10);

  private final ArticleViewDistributedRepository articleViewDistributedRepository;
  private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;
  private final ArticleViewCountRepository articleViewCountRepository;

  public Long increase(Long articleId, Long userId) {
    // 락 획득
    if (!articleViewDistributedRepository.lock(articleId, userId, TTL)) {
      return articleViewCountRepository.read(articleId);
    }

    Long count = articleViewCountRepository.increase(articleId);
    if (count % BACK_UP_BATCH_SIZE == 0) {
      articleViewCountBackUpProcessor.backup(articleId, count);
    }
    return count;
  }

  public Long count(Long articleId) {
    return articleViewCountRepository.read(articleId);
  }

}
