package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

    Optional<Content> findByTitleKoAndCategory(String titleKo, ContentCategory category);

    List<Content> findByCategoryOrderByIdDesc(ContentCategory category);

    List<Content> findAllByOrderByIdDesc();
}
