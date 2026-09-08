package com.Wavey.WaveyService.domain.content.external.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class YoutubeDataClientTest {

    private static final String VIDEO_ID = "GIAnKeKXzGU";
    private static final String API_KEY = "test-youtube-api-key";

    private YoutubeDataClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new YoutubeDataClient(builder);
        ReflectionTestUtils.setField(client, "baseUrl", "https://www.googleapis.com/youtube/v3");
        ReflectionTestUtils.setField(client, "apiKey", API_KEY);
    }

    @Test
    void 영상_조회시_snippet에서_제목과_썸네일을_가져온다() {
        server.expect(requestTo(containsString("/youtube/v3/videos")))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "items": [
                            {
                              "id": "%s",
                              "snippet": {
                                "title": "도깨비 하이라이트",
                                "description": "tvN 하이라이트",
                                "channelTitle": "tvN DRAMA",
                                "thumbnails": {
                                  "high": { "url": "https://i.ytimg.com/vi/%s/hqdefault.jpg" }
                                }
                              }
                            }
                          ]
                        }
                        """.formatted(VIDEO_ID, VIDEO_ID), APPLICATION_JSON));

        YoutubeVideoDetails details = client.fetchVideo(VIDEO_ID);

        assertThat(details.videoId()).isEqualTo(VIDEO_ID);
        assertThat(details.title()).isEqualTo("도깨비 하이라이트");
        assertThat(details.description()).isEqualTo("tvN 하이라이트");
        assertThat(details.channelTitle()).isEqualTo("tvN DRAMA");
        assertThat(details.thumbnailUrl()).isEqualTo("https://i.ytimg.com/vi/" + VIDEO_ID + "/hqdefault.jpg");
        assertThat(details.durationSec()).isNull();
        server.verify();
    }

    @Test
    void 영상_조회시_ISO_길이를_초로_바꾼다() {
        server.expect(requestTo(containsString("/youtube/v3/videos")))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "items": [
                            {
                              "id": "%s",
                              "snippet": { "title": "도깨비 예고편", "thumbnails": { "default": { "url": "https://img" } } },
                              "contentDetails": { "duration": "PT1M30S" }
                            }
                          ]
                        }
                        """.formatted(VIDEO_ID), APPLICATION_JSON));

        YoutubeVideoDetails details = client.fetchVideo(VIDEO_ID);

        assertThat(details.durationSec()).isEqualTo(90);
        server.verify();
    }

    @Test
    void 검색은_videoId만_모은다() {
        server.expect(requestTo(containsString("/youtube/v3/search")))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "items": [
                            { "id": { "videoId": "%s" } },
                            { "id": { "videoId": "%s" } }
                          ]
                        }
                        """.formatted(VIDEO_ID, VIDEO_ID), APPLICATION_JSON));

        assertThat(client.searchVideoIds("도깨비 예고편", 10)).containsExactly(VIDEO_ID);
        server.verify();
    }

    @Test
    void items가_비면_영상을_찾지_못한_것으로_본다() {
        server.expect(requestTo(containsString("/youtube/v3/videos")))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        { "items": [] }
                        """, APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchVideo(VIDEO_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.YOUTUBE_VIDEO_NOT_FOUND);
        server.verify();
    }

    @Test
    void API_키가_없으면_유튜브를_호출하지_않는다() {
        ReflectionTestUtils.setField(client, "apiKey", "");

        assertThatThrownBy(() -> client.fetchVideo(VIDEO_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.YOUTUBE_API_KEY_MISSING);
        server.verify();
    }

    @Test
    void 유튜브가_오류_상태코드를_주면_요청_실패로_본다() {
        server.expect(requestTo(containsString("/youtube/v3/videos")))
                .andExpect(method(GET))
                .andRespond(withStatus(FORBIDDEN));

        assertThatThrownBy(() -> client.fetchVideo(VIDEO_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.YOUTUBE_API_REQUEST_FAILED);
        server.verify();
    }
}
