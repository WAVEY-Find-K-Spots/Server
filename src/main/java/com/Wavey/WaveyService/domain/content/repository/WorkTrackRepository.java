package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.WorkTrack;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkTrackRepository extends JpaRepository<WorkTrack, Long> {

    List<WorkTrack> findByWorkIdAndHiddenFalseOrderByIdAsc(Long workId);

    List<WorkTrack> findByWorkId(Long workId);

    Optional<WorkTrack> findByWorkIdAndSpotifyId(Long workId, String spotifyId);

    void deleteByWorkIdAndHiddenFalse(Long workId);

    void deleteByWorkIdAndHiddenFalseAndSpotifyIdNotIn(Long workId, Collection<String> spotifyIds);
}
