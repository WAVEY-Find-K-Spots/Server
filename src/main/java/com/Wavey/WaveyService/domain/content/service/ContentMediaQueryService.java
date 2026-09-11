package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.dto.ContentTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentVideoResponse;
import com.Wavey.WaveyService.domain.content.entity.ContentTrack;
import com.Wavey.WaveyService.domain.content.entity.ContentVideo;
import com.Wavey.WaveyService.domain.content.repository.ContentTrackRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentVideoRepository;
import com.Wavey.WaveyService.domain.content.service.ContentService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentMediaQueryService {

    private final ContentService workService;
    private final ContentVideoRepository workVideoRepository;
    private final ContentTrackRepository workTrackRepository;

    @Transactional(readOnly = true)
    public List<ContentVideoResponse> listVideos(Long contentId) {
        workService.getContent(contentId);
        return workVideoRepository.findByContentIdAndHiddenFalseOrderByIdAsc(contentId).stream()
                .map(ContentVideoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContentTrackResponse> listTracks(Long contentId) {
        workService.getContent(contentId);
        return workTrackRepository.findByContentIdAndHiddenFalseOrderByIdAsc(contentId).stream()
                .map(ContentTrackResponse::from)
                .toList();
    }

    @Transactional
    public ContentVideoResponse updateVideoHidden(Long videoId, boolean hidden) {
        ContentVideo video = workVideoRepository.findById(videoId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_VIDEO_NOT_FOUND));
        video.updateHidden(hidden);
        return ContentVideoResponse.from(video);
    }

    @Transactional
    public ContentTrackResponse updateTrackHidden(Long trackId, boolean hidden) {
        ContentTrack track = workTrackRepository.findById(trackId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_TRACK_NOT_FOUND));
        track.updateHidden(hidden);
        return ContentTrackResponse.from(track);
    }
}
