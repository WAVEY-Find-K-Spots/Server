package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.WorkVideo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkVideoRepository extends JpaRepository<WorkVideo, Long> {

    List<WorkVideo> findByWorkIdAndHiddenFalseOrderByIdAsc(Long workId);

    List<WorkVideo> findByWorkId(Long workId);

    Optional<WorkVideo> findByWorkIdAndYoutubeVideoId(Long workId, String youtubeVideoId);

    void deleteByWorkIdAndHiddenFalse(Long workId);

    void deleteByWorkIdAndHiddenFalseAndYoutubeVideoIdNotIn(Long workId, Collection<String> youtubeVideoIds);
}
