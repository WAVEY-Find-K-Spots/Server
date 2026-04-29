package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByPlatformAndTitleContainingIgnoreCase(ContentPlatform platform, String title);

    Optional<Content> findByPlatformAndExternalId(ContentPlatform platform, String externalId);
}
