package com.Wavey.WaveyService.domain.work.service;

import com.Wavey.WaveyService.domain.work.dto.WorkRequest;
import com.Wavey.WaveyService.domain.work.dto.WorkResponse;
import com.Wavey.WaveyService.domain.work.entity.Work;
import com.Wavey.WaveyService.domain.work.entity.WorkType;
import com.Wavey.WaveyService.domain.work.repository.WorkRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;

    @Transactional
    public WorkResponse create(WorkRequest request) {
        String title = request.getTitle().trim();
        if (workRepository.findByTitleAndType(title, request.getType()).isPresent()) {
            throw new CustomException(ErrorCode.WORK_ALREADY_EXISTS);
        }

        Work work = Work.builder()
                .title(title)
                .titleEn(trimToNull(request.getTitleEn()))
                .type(request.getType())
                .artistName(trimToNull(request.getArtistName()))
                .build();

        return WorkResponse.from(workRepository.save(work));
    }

    @Transactional(readOnly = true)
    public WorkResponse get(Long workId) {
        return WorkResponse.from(getWork(workId));
    }

    @Transactional(readOnly = true)
    public List<WorkResponse> list(WorkType type) {
        List<Work> works = type == null
                ? workRepository.findAllByOrderByIdDesc()
                : workRepository.findByTypeOrderByIdDesc(type);
        return works.stream().map(WorkResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Work getWork(Long workId) {
        return workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
