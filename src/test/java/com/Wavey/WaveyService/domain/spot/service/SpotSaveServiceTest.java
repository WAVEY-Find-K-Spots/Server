package com.Wavey.WaveyService.domain.spot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.Wavey.WaveyService.domain.spot.dto.response.SpotSaveResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SavedSpotRepository;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
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
class SpotSaveServiceTest {

    @Mock
    private SavedSpotRepository savedSpotRepository;
    @Mock
    private SpotRepository spotRepository;

    @InjectMocks
    private SpotSaveService service;

    @Test
    void save_new_incrementsSavedCount() {
        Spot spot = spot(1L, 0L);
        given(spotRepository.findById(1L)).willReturn(Optional.of(spot));
        given(savedSpotRepository.existsByUserIdAndSpotId(10L, 1L)).willReturn(false);

        SpotSaveResponse result = service.save(1L, 10L);

        assertThat(result.saved()).isTrue();
        assertThat(spot.getSavedCount()).isEqualTo(1L);
    }

    @Test
    void save_alreadySaved_isIdempotent() {
        Spot spot = spot(1L, 3L);
        given(spotRepository.findById(1L)).willReturn(Optional.of(spot));
        given(savedSpotRepository.existsByUserIdAndSpotId(10L, 1L)).willReturn(true);

        SpotSaveResponse result = service.save(1L, 10L);

        assertThat(result.saved()).isTrue();
        assertThat(spot.getSavedCount()).isEqualTo(3L);
        verify(savedSpotRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unsave_existing_decrementsSavedCount() {
        Spot spot = spot(1L, 3L);
        given(spotRepository.findById(1L)).willReturn(Optional.of(spot));
        given(savedSpotRepository.existsByUserIdAndSpotId(10L, 1L)).willReturn(true);

        SpotSaveResponse result = service.unsave(1L, 10L);

        assertThat(result.saved()).isFalse();
        assertThat(spot.getSavedCount()).isEqualTo(2L);
    }

    @Test
    void save_missingSpot_throwsNotFound() {
        given(spotRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(99L, 10L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SPOT_NOT_FOUND);
    }

    private static Spot spot(Long id, long savedCount) {
        Spot spot = Spot.builder().savedCount(savedCount).build();
        ReflectionTestUtils.setField(spot, "id", id);
        return spot;
    }
}
