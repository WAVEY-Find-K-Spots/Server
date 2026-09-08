package com.Wavey.WaveyService.domain.spot.external.service;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import com.Wavey.WaveyService.domain.spot.external.support.ExternalSpotRegionResolver;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MediaLocationCsvImportService {

    private static final Charset CSV_CHARSET = Charset.forName("MS949");
    private static final String DEFAULT_CSV_PATH =
            "C:/Users/joo/Desktop/한국문화정보원_미디어콘텐츠 영상 촬영지 데이터_20221125.csv";

    private final SpotRepository spotRepository;
    private final ExternalSpotRegionResolver regionResolver;

    @Value("${spot.sync.media-location-csv-path:" + DEFAULT_CSV_PATH + "}")
    private String csvPath;

    @Transactional
    public CsvImportSummary importCsv() {
        Path path = Paths.get(csvPath);
        if (!Files.exists(path)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        List<CsvRow> rows = readCsv(path);
        Map<String, Spot> existingSpots = new HashMap<>();
        spotRepository
                .findAllBySourceType(SpotSourceType.MEDIA_LOCATION_DATA)
                .forEach(spot -> existingSpots.put(spot.getExternalContentId(), spot));

        int savedCount = 0;
        int updatedCount = 0;
        int unchangedCount = 0;
        int skippedCount = 0;

        for (CsvRow row : rows) {
            if (!isSavable(row)) {
                skippedCount++;
                continue;
            }

            Optional<Long> regionId = regionResolver.resolveRegionId(row.getAddress());
            if (regionId.isEmpty()) {
                skippedCount++;
                continue;
            }

            SpotCategory category = toCategory(row.getMediaType());
            if (category == null) {
                skippedCount++;
                continue;
            }

            Spot existingSpot = existingSpots.get(row.getSequence());
            if (existingSpot == null) {
                spotRepository.save(toEntity(row, regionId.get(), category));
                savedCount++;
                continue;
            }

            boolean changed =
                    existingSpot.updateFromExternal(
                            regionId.get(),
                            row.getMediaType(),
                            row.getTitle(),
                            row.getName(),
                            row.getPlaceType(),
                            category,
                            row.getAddress(),
                            row.getLatitude(),
                            row.getLongitude(),
                            row.getDescription(),
                            row.getOpeningHours(),
                            row.getBreakTime(),
                            row.getClosedDays(),
                            row.getTel(),
                            existingSpot.getThumbnailUrl(),
                            row.getSourceUpdatedAt());

            if (changed) {
                updatedCount++;
            } else {
                unchangedCount++;
            }
        }

        return CsvImportSummary.builder()
                .requestedCount(rows.size())
                .savedCount(savedCount)
                .updatedCount(updatedCount)
                .unchangedCount(unchangedCount)
                .skippedCount(skippedCount)
                .build();
    }

    private List<CsvRow> readCsv(Path path) {
        List<CsvRow> rows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, CSV_CHARSET)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.hasText(line)) {
                    continue;
                }

                List<String> columns = parseCsvLine(line);
                if (columns.size() < 14) {
                    continue;
                }

                rows.add(
                        CsvRow.builder()
                                .sequence(clean(columns.get(0)))
                                .mediaType(clean(columns.get(1)))
                                .title(clean(columns.get(2)))
                                .name(clean(columns.get(3)))
                                .placeType(clean(columns.get(4)))
                                .description(clean(columns.get(5)))
                                .openingHours(clean(columns.get(6)))
                                .breakTime(clean(columns.get(7)))
                                .closedDays(clean(columns.get(8)))
                                .address(clean(columns.get(9)))
                                .latitude(toBigDecimal(columns.get(10)))
                                .longitude(toBigDecimal(columns.get(11)))
                                .tel(clean(columns.get(12)))
                                .sourceUpdatedAt(toLocalDate(columns.get(13)))
                                .build());
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return rows;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        values.add(current.toString());
        return values;
    }

    private Spot toEntity(CsvRow row, Long regionId, SpotCategory category) {
        return Spot.builder()
                .regionId(regionId)
                .mediaType(row.getMediaType())
                .title(row.getTitle())
                .name(row.getName())
                .placeType(row.getPlaceType())
                .category(category)
                .address(row.getAddress())
                .latitude(row.getLatitude())
                .longitude(row.getLongitude())
                .description(row.getDescription())
                .openingHours(row.getOpeningHours())
                .breakTime(row.getBreakTime())
                .closedDays(row.getClosedDays())
                .tel(row.getTel())
                .sourceUpdatedAt(row.getSourceUpdatedAt())
                .sourceType(SpotSourceType.MEDIA_LOCATION_DATA)
                .externalContentId(row.getSequence())
                .avgRating(0.0)
                .build();
    }

    private boolean isSavable(CsvRow row) {
        return row != null
                && StringUtils.hasText(row.getSequence())
                && StringUtils.hasText(row.getMediaType())
                && StringUtils.hasText(row.getName())
                && StringUtils.hasText(row.getAddress())
                && row.getLatitude() != null
                && row.getLongitude() != null;
    }

    private SpotCategory toCategory(String mediaType) {
        if (!StringUtils.hasText(mediaType)) {
            return null;
        }

        return switch (mediaType.trim().toLowerCase(Locale.ROOT)) {
            case "drama" -> SpotCategory.K_DRAMA;
            case "artist", "show" -> SpotCategory.K_POP;
            case "movie" -> SpotCategory.K_MOVIE;
            default -> null;
        };
    }

    private String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() || Objects.equals(trimmed, "정보없음") ? null : trimmed;
    }

    private BigDecimal toBigDecimal(String value) {
        String cleaned = clean(value);
        if (!StringUtils.hasText(cleaned)) {
            return null;
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate toLocalDate(String value) {
        String cleaned = clean(value);
        if (!StringUtils.hasText(cleaned)) {
            return null;
        }

        try {
            return LocalDate.parse(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    @Getter
    @Builder
    public static class CsvImportSummary {
        private int requestedCount;
        private int savedCount;
        private int updatedCount;
        private int unchangedCount;
        private int skippedCount;
    }

    @Getter
    @Builder
    private static class CsvRow {
        private String sequence;
        private String mediaType;
        private String title;
        private String name;
        private String placeType;
        private String description;
        private String openingHours;
        private String breakTime;
        private String closedDays;
        private String address;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String tel;
        private LocalDate sourceUpdatedAt;
    }
}
