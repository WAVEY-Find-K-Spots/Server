package com.Wavey.WaveyService.domain.spot.sync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.spot.sync.client.SpotWikimediaImageClient;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotWikimediaEnrichResponse;
import com.Wavey.WaveyService.domain.upload.service.UploadService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class SpotWikimediaEnrichmentServiceTest {

    @Mock
    private SpotRepository spotRepository;
    @Mock
    private SpotWikimediaImageClient wikimediaImageClient;
    @Mock
    private UploadService uploadService;

    @InjectMocks
    private SpotWikimediaEnrichmentService service;

    @Test
    void 검색결과가_없으면_건너뛴다() {
        ReflectionTestUtils.setField(service, "restClientBuilder", RestClient.builder());
        Spot spot = spot(1L, "무명카페");
        given(spotRepository.findMissingImage(any(Pageable.class))).willReturn(List.of(spot));
        given(wikimediaImageClient.findImage(eq("무명카페"), any())).willReturn(Optional.empty());

        SpotWikimediaEnrichResponse result = service.enrichImages(10);

        assertThat(result.getRequested()).isEqualTo(1);
        assertThat(result.getFilled()).isEqualTo(0);
        assertThat(result.getSkipped()).isEqualTo(1);
        verify(uploadService, never()).uploadServerSide(any(), any(), any(), any());
    }

    private static Spot spot(Long id, String nameKo) {
        Spot spot = Spot.builder().nameKo(nameKo).build();
        ReflectionTestUtils.setField(spot, "id", id);
        return spot;
    }
}
