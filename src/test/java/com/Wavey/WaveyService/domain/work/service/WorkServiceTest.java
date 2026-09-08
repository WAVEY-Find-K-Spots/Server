package com.Wavey.WaveyService.domain.work.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.work.dto.WorkRequest;
import com.Wavey.WaveyService.domain.work.dto.WorkResponse;
import com.Wavey.WaveyService.domain.work.entity.Work;
import com.Wavey.WaveyService.domain.work.entity.WorkType;
import com.Wavey.WaveyService.domain.work.repository.WorkRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private WorkService workService;

    @Test
    void 작품을_등록한다() {
        given(workRepository.findByTitleAndType("도깨비", WorkType.DRAMA)).willReturn(Optional.empty());
        given(workRepository.save(any(Work.class))).willAnswer(invocation -> {
            Work work = invocation.getArgument(0);
            ReflectionTestUtils.setField(work, "id", 1L);
            return work;
        });

        WorkResponse response = workService.create(WorkRequest.builder()
                .title("도깨비")
                .titleEn("Guardian")
                .type(WorkType.DRAMA)
                .build());

        assertThat(response.getWorkId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("도깨비");
        assertThat(response.getTitleEn()).isEqualTo("Guardian");
        verify(workRepository).save(any(Work.class));
    }

    @Test
    void 같은_제목과_유형이면_등록을_거절한다() {
        given(workRepository.findByTitleAndType("도깨비", WorkType.DRAMA))
                .willReturn(Optional.of(Work.builder().title("도깨비").type(WorkType.DRAMA).build()));

        assertThatThrownBy(() -> workService.create(WorkRequest.builder()
                .title("도깨비")
                .type(WorkType.DRAMA)
                .build()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WORK_ALREADY_EXISTS);
    }
}
