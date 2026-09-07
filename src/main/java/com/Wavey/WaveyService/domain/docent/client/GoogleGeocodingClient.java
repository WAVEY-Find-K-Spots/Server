package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.domain.docent.model.AdministrativeArea;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

/** Google Geocoding API v3를 이용해 좌표를 한국 행정구역으로 변환합니다. */
@Component
public class GoogleGeocodingClient implements GeocodingClient {

    private final RestClient restClient;
    private final String apiKey;

    public GoogleGeocodingClient(
            RestClient.Builder restClientBuilder,
            @Value("${heritage.google-geocoding.base-url:https://maps.googleapis.com}") String baseUrl,
            @Value("${heritage.google-geocoding.api-key:}") String apiKey
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public Optional<AdministrativeArea> reverseGeocode(double latitude, double longitude) {
        validate(latitude, longitude);
        try {
            GeocodingResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/geocode/json")
                            .queryParam("latlng", latitude + "," + longitude)
                            .queryParam("language", "ko")
                            .queryParam("region", "kr")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GeocodingResponse.class);

            if (response == null || response.status() == null) {
                throw new CustomException(ErrorCode.GOOGLE_GEOCODING_FAILED);
            }
            if ("ZERO_RESULTS".equals(response.status())) {
                return Optional.empty();
            }
            if ("OVER_QUERY_LIMIT".equals(response.status())) {
                throw new CustomException(ErrorCode.GOOGLE_GEOCODING_QUOTA_EXHAUSTED);
            }
            if (!"OK".equals(response.status()) || response.results() == null
                    || response.results().isEmpty()) {
                throw new CustomException(ErrorCode.GOOGLE_GEOCODING_FAILED);
            }

            GeocodingResult result = response.results().getFirst();
            String region1 = component(result, "administrative_area_level_1").orElse("");
            String region2 = selectRegion2(result, region1);
            return Optional.of(new AdministrativeArea(
                    region1,
                    region2,
                    result.formattedAddress()
            ));
        } catch (CustomException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 429) {
                throw new CustomException(ErrorCode.GOOGLE_GEOCODING_QUOTA_EXHAUSTED);
            }
            throw new CustomException(ErrorCode.GOOGLE_GEOCODING_FAILED);
        } catch (RestClientException exception) {
            throw new CustomException(ErrorCode.GOOGLE_GEOCODING_FAILED);
        }
    }

    private String selectRegion2(GeocodingResult result, String region1) {
        if (region1.endsWith("도")) {
            return component(result, "locality")
                    .or(() -> component(result, "administrative_area_level_2"))
                    .or(() -> component(result, "sublocality_level_1"))
                    .orElse("");
        }
        return component(result, "sublocality_level_1")
                .or(() -> component(result, "locality"))
                .or(() -> component(result, "administrative_area_level_2"))
                .orElse("");
    }

    private Optional<String> component(GeocodingResult result, String type) {
        if (result.addressComponents() == null) {
            return Optional.empty();
        }
        return result.addressComponents().stream()
                .filter(component -> component.types() != null && component.types().contains(type))
                .map(AddressComponent::longName)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private void validate(double latitude, double longitude) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException(ErrorCode.GOOGLE_GEOCODING_CONFIGURATION_MISSING);
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodingResponse(
            String status,
            @JsonProperty("error_message") String errorMessage,
            List<GeocodingResult> results
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodingResult(
            @JsonProperty("formatted_address") String formattedAddress,
            @JsonProperty("address_components") List<AddressComponent> addressComponents
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AddressComponent(
            @JsonProperty("long_name") String longName,
            @JsonProperty("short_name") String shortName,
            List<String> types
    ) {
    }
}