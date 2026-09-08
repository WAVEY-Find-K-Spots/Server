package com.Wavey.WaveyService.domain.content.external.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.Wavey.WaveyService.domain.content.external.dto.SpotifyTrackDetails;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SpotifyApiClientTest {

    @Test
    void 트랙_조회시_토큰을_받은_뒤_곡_정보를_가져온다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        SpotifyApiClient client = new SpotifyApiClient(builder);
        ReflectionTestUtils.setField(client, "clientId", "test-client-id");
        ReflectionTestUtils.setField(client, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(client, "tokenUrl", "https://accounts.spotify.com/api/token");
        ReflectionTestUtils.setField(client, "baseUrl", "https://api.spotify.com/v1");

        server.expect(requestTo("https://accounts.spotify.com/api/token"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {"access_token":"test-token","token_type":"Bearer","expires_in":3600}
                        """, APPLICATION_JSON));

        server.expect(requestTo("https://api.spotify.com/v1/tracks/3n3Ppam7vgaVa1iaRUc9Lp?market=KR"))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("""
                        {
                          "id": "3n3Ppam7vgaVa1iaRUc9Lp",
                          "name": "Stay With Me",
                          "artists": [{"name": "CHANYEOL"}, {"name": "Punch"}],
                          "album": {
                            "name": "도깨비 OST Part 1",
                            "images": [{"url": "https://i.scdn.co/image/cover"}]
                          }
                        }
                        """, APPLICATION_JSON));

        SpotifyTrackDetails details = client.fetchTrack("3n3Ppam7vgaVa1iaRUc9Lp");

        assertThat(details.trackId()).isEqualTo("3n3Ppam7vgaVa1iaRUc9Lp");
        assertThat(details.title()).isEqualTo("Stay With Me");
        assertThat(details.artistName()).isEqualTo("CHANYEOL, Punch");
        assertThat(details.albumName()).isEqualTo("도깨비 OST Part 1");
        assertThat(details.thumbnailUrl()).isEqualTo("https://i.scdn.co/image/cover");
        server.verify();
    }

    @Test
    void 트랙_검색은_플레이리스트를_치지_않는다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        SpotifyApiClient client = new SpotifyApiClient(builder);
        ReflectionTestUtils.setField(client, "clientId", "test-client-id");
        ReflectionTestUtils.setField(client, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(client, "tokenUrl", "https://accounts.spotify.com/api/token");
        ReflectionTestUtils.setField(client, "baseUrl", "https://api.spotify.com/v1");

        server.expect(requestTo("https://accounts.spotify.com/api/token"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {"access_token":"test-token","token_type":"Bearer","expires_in":3600}
                        """, APPLICATION_JSON));

        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v1/search")))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("""
                        {
                          "tracks": {
                            "items": [
                              {
                                "id": "stay1",
                                "name": "Stay With Me",
                                "duration_ms": 198000,
                                "artists": [{"name": "CHANYEOL"}],
                                "album": {
                                  "id": "album-1",
                                  "name": "Guardian (Original Television Soundtrack), Pt. 1",
                                  "images": [{"url": "https://i.scdn.co/image/cover"}]
                                }
                              }
                            ]
                          }
                        }
                        """, APPLICATION_JSON));

        var tracks = client.searchTracks("도깨비 OST", 10);

        assertThat(tracks).hasSize(1);
        assertThat(tracks.get(0).title()).isEqualTo("Stay With Me");
        assertThat(tracks.get(0).albumId()).isEqualTo("album-1");
        server.verify();
    }
}
