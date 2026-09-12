package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.ContentVideo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentVideoRepository extends JpaRepository<ContentVideo, Long> {

    List<ContentVideo> findByContentIdAndHiddenFalseOrderByIdAsc(Long contentId);

    List<ContentVideo> findByContentIdInAndHiddenFalseOrderByIdAsc(Collection<Long> contentIds);

    List<ContentVideo> findByContentId(Long contentId);

    Optional<ContentVideo> findByContentIdAndYoutubeVideoId(Long contentId, String youtubeVideoId);

    void deleteByContentId(Long contentId);

    void deleteByContentIdAndHiddenFalse(Long contentId);

    void deleteByContentIdAndHiddenFalseAndYoutubeVideoIdNotIn(Long contentId, Collection<String> youtubeVideoIds);
}
