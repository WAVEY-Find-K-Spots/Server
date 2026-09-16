package com.Wavey.WaveyService.domain.spot.sync.service;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.spot.sync.client.SpotWikimediaImageClient;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotWikimediaEnrichResponse;
import com.Wavey.WaveyService.domain.upload.enums.UploadCategory;
import com.Wavey.WaveyService.domain.upload.service.UploadService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotWikimediaEnrichmentService {

    private final SpotRepository spotRepository;
    private final SpotWikimediaImageClient wikimediaImageClient;
    private final UploadService uploadService;
    private final RestClient.Builder restClientBuilder;

    @Transactional
    public SpotWikimediaEnrichResponse enrichImages(int limit) {
        List<Spot> candidates = spotRepository.findMissingImage(PageRequest.of(0, Math.max(0, limit)));

        int filled = 0;
        int skipped = 0;
        for (Spot spot : candidates) {
            try {
                Optional<SpotWikimediaImageClient.WikimediaImage> image =
                        wikimediaImageClient.findImage(spot.getNameKo(), spot.getCategory());
                if (image.isEmpty()) {
                    skipped++;
                    continue;
                }

                ResponseEntity<byte[]> downloaded = downloadImage(image.get().imageUrl());
                byte[] bytes = downloaded.getBody();
                String contentType = resolveContentType(downloaded);
                if (bytes == null || bytes.length == 0
                        || contentType == null || UploadCategory.SPOT.extensionFor(contentType) == null) {
                    skipped++;
                    continue;
                }

                String hostedUrl = uploadService.uploadServerSide(
                        UploadCategory.SPOT, spot.getSpotId(), bytes, contentType);
                spot.updateImage(hostedUrl, image.get().attribution());
                filled++;
            } catch (Exception e) {
                log.warn("Wikimedia image enrichment failed for spotId={}", spot.getSpotId(), e);
                skipped++;
            }
        }

        return SpotWikimediaEnrichResponse.builder()
                .requested(candidates.size())
                .filled(filled)
                .skipped(skipped)
                .build();
    }

    private ResponseEntity<byte[]> downloadImage(String url) {
        return restClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.USER_AGENT, "WaveyService/1.0 (K-content spot photo enrichment)")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<byte[]>() {});
    }

    private String resolveContentType(ResponseEntity<byte[]> response) {
        MediaType mediaType = response.getHeaders().getContentType();
        return mediaType == null ? null : mediaType.getType() + "/" + mediaType.getSubtype();
    }
}
