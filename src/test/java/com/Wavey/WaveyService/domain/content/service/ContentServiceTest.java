package com.Wavey.WaveyService.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.Wavey.WaveyService.domain.content.dto.ContentRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentResponse;
import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentPlatform;
import com.Wavey.WaveyService.domain.content.external.client.YoutubeDataClient;
import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import com.Wavey.WaveyService.domain.content.repository.ContentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private YoutubeDataClient youtubeDataClient;

    @InjectMocks
    private ContentService contentService;

    @Test
    void 유튜브_등록시_제목이_없으면_Data_API_제목을_저장한다() {
        given(contentRepository.findByPlatformAndExternalId(ContentPlatform.YOUTUBE, "GIAnKeKXzGU"))
                .willReturn(Optional.empty());
        given(youtubeDataClient.fetchVideo("GIAnKeKXzGU")).willReturn(new YoutubeVideoDetails(
                "GIAnKeKXzGU",
                "도깨비 하이라이트",
                "tvN 하이라이트",
                "https://i.ytimg.com/vi/GIAnKeKXzGU/hqdefault.jpg",
                "tvN DRAMA"
        ));
        given(contentRepository.save(any(Content.class))).willAnswer(invocation -> {
            Content content = invocation.getArgument(0);
            ReflectionTestUtils.setField(content, "id", 1L);
            return content;
        });

        ContentResponse response = contentService.createYoutube(ContentRequest.builder()
                .url("https://www.youtube.com/watch?v=GIAnKeKXzGU")
                .build());

        assertThat(response.getContentId()).isEqualTo(1L);
        assertThat(response.getPlatform()).isEqualTo(ContentPlatform.YOUTUBE);
        assertThat(response.getTitle()).isEqualTo("도깨비 하이라이트");
        assertThat(response.getDescription()).isEqualTo("tvN 하이라이트");
        assertThat(response.getExternalId()).isEqualTo("GIAnKeKXzGU");
        assertThat(response.getThumbnailUrl()).isEqualTo("https://i.ytimg.com/vi/GIAnKeKXzGU/hqdefault.jpg");
    }
}
