package keku.board.view.service;

import keku.board.view.entity.ArticleViewCount;
import keku.board.view.repository.ArticleViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {

  private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;

  @Transactional
  public void backup(Long articleId, Long viewCount){
    int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);
    if (result == 0) {
      articleViewCountBackUpRepository.findById(articleId)
          .ifPresentOrElse(ignore -> { },
              () -> articleViewCountBackUpRepository.save(ArticleViewCount.init(articleId, viewCount)));
    }
  }

}
