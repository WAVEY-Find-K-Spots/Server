package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.SpotContent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotContentRepository extends JpaRepository<SpotContent, Long> {
    List<SpotContent> findBySpotIdOrderByIdAsc(Long spotId);

    void deleteByContentId(Long contentId);
}
