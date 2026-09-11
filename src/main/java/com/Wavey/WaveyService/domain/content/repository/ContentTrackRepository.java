package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.ContentTrack;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentTrackRepository extends JpaRepository<ContentTrack, Long> {

    List<ContentTrack> findByContentIdAndHiddenFalseOrderByIdAsc(Long contentId);

    List<ContentTrack> findByContentId(Long contentId);

    Optional<ContentTrack> findByContentIdAndSpotifyTrackId(Long contentId, String spotifyTrackId);

    void deleteByContentIdAndHiddenFalse(Long contentId);

    void deleteByContentIdAndHiddenFalseAndSpotifyTrackIdNotIn(Long contentId, Collection<String> spotifyTrackIds);
}
