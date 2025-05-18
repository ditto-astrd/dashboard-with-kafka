package keku.board.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleViewCountRepository {
  // view::article::{article_id}::view_count
  private static final String KEY_FORMAT = "view::article::%s::view_count";
  private final StringRedisTemplate redisTemplate;

  public Long read(Long articleId) {
    String result = redisTemplate.opsForValue().get(generateKey(articleId));
    return result == null ? 0L : Long.valueOf(result);
  }

  private String generateKey(Long articleId) {
    return KEY_FORMAT.formatted(articleId);
  }

  public Long increase(Long articleId) {
    return redisTemplate.opsForValue().increment(generateKey(articleId));
  }
}
