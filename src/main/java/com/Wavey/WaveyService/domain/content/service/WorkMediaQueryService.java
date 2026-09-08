package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.dto.WorkTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.WorkVideoResponse;
import com.Wavey.WaveyService.domain.content.entity.WorkTrack;
import com.Wavey.WaveyService.domain.content.entity.WorkVideo;
import com.Wavey.WaveyService.domain.content.repository.WorkTrackRepository;
import com.Wavey.WaveyService.domain.content.repository.WorkVideoRepository;
import com.Wavey.WaveyService.domain.work.service.WorkService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkMediaQueryService {

    private final WorkService workService;
    private final WorkVideoRepository workVideoRepository;
    private final WorkTrackRepository workTrackRepository;

    @Transactional(readOnly = true)
    public List<WorkVideoResponse> listVideos(Long workId) {
        workService.getWork(workId);
        return workVideoRepository.findByWorkIdAndHiddenFalseOrderByIdAsc(workId).stream()
                .map(WorkVideoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkTrackResponse> listTracks(Long workId) {
        workService.getWork(workId);
        return workTrackRepository.findByWorkIdAndHiddenFalseOrderByIdAsc(workId).stream()
                .sorted(Comparator.comparing(WorkTrack::isPreviewAvailable).reversed()
                        .thenComparing(WorkTrack::getWorkTrackId, Comparator.nullsLast(Long::compareTo)))
                .map(WorkTrackResponse::from)
                .toList();
    }

    @Transactional
    public WorkVideoResponse updateVideoHidden(Long videoId, boolean hidden) {
        WorkVideo video = workVideoRepository.findById(videoId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_VIDEO_NOT_FOUND));
        video.updateHidden(hidden);
        return WorkVideoResponse.from(video);
    }

    @Transactional
    public WorkTrackResponse updateTrackHidden(Long trackId, boolean hidden) {
        WorkTrack track = workTrackRepository.findById(trackId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_TRACK_NOT_FOUND));
        track.updateHidden(hidden);
        return WorkTrackResponse.from(track);
    }
}
