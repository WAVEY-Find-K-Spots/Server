package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.domain.docent.model.AdministrativeArea;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleGeocodingClientTest {

    @Test
    void 경복궁_좌표를_서울특별시와_종로구로_변환한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GoogleGeocodingClient client = new GoogleGeocodingClient(
                builder,
                "https://maps.googleapis.com",
                "test-api-key"
        );

        server.expect(requestTo(containsString(
                        "/maps/api/geocode/json?latlng=37.579617"
                )))
                .andRespond(withSuccess("""
                        {
                          "status": "OK",
                          "results": [
                            {
                              "formatted_address": "대한민국 서울특별시 종로구 세종로 1-1",
                              "address_components": [
                                {
                                  "long_name": "종로구",
                                  "short_name": "종로구",
                                  "types": ["political", "sublocality", "sublocality_level_1"]
                                },
                                {
                                  "long_name": "서울특별시",
                                  "short_name": "서울특별시",
                                  "types": ["administrative_area_level_1", "political"]
                                }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<AdministrativeArea> result = client.reverseGeocode(
                37.579617,
                126.977041
        );

        assertThat(result).contains(new AdministrativeArea(
                "서울특별시",
                "종로구",
                "대한민국 서울특별시 종로구 세종로 1-1"
        ));
        server.verify();
    }

    @Test
    void API_Key가_없으면_요청하지_않는다() {
        GoogleGeocodingClient client = new GoogleGeocodingClient(
                RestClient.builder(),
                "https://maps.googleapis.com",
                ""
        );

        assertThatThrownBy(() -> client.reverseGeocode(37.579617, 126.977041))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GOOGLE_GEOCODING_CONFIGURATION_MISSING);
    }
}