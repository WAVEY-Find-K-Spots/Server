package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.SpotContent;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface SpotContentRepository extends JpaRepository<SpotContent, Long> {
    List<SpotContent> findBySpotIdOrderByDisplayOrderAscIdAsc(Long spotId);
}
