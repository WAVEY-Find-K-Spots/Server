package com.Wavey.WaveyService.domain.content.repository;

import com.Wavey.WaveyService.domain.content.entity.ContentAlbum;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentAlbumRepository extends JpaRepository<ContentAlbum, Long> {

    List<ContentAlbum> findByContentIdAndHiddenFalseOrderByIdAsc(Long contentId);

    List<ContentAlbum> findByContentIdInAndHiddenFalseOrderByIdAsc(Collection<Long> contentIds);

    List<ContentAlbum> findByContentId(Long contentId);

    Optional<ContentAlbum> findByContentIdAndSpotifyAlbumId(Long contentId, String spotifyAlbumId);

    void deleteByContentId(Long contentId);

    void deleteByContentIdAndHiddenFalse(Long contentId);

    void deleteByContentIdAndHiddenFalseAndSpotifyAlbumIdNotIn(Long contentId, Collection<String> spotifyAlbumIds);
}
