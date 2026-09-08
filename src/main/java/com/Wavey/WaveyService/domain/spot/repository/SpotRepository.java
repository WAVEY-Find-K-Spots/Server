package com.Wavey.WaveyService.domain.spot.repository;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long>, JpaSpecificationExecutor<Spot> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Spot s where s.id = :id")
    Optional<Spot> findLockedById(Long id);

    List<Spot> findAllBySourceType(SpotSourceType sourceType);

    @Query(
            """
            SELECT s
            FROM Spot s
            WHERE (:category IS NULL OR s.category = :category)
              AND (:regionId IS NULL OR s.regionId = :regionId)
            """)
    List<Spot> search(@Param("category") SpotCategory category, @Param("regionId") Long regionId);

    @Query(
            """
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Spot s
            WHERE s.sourceType = :sourceType
              AND s.externalContentId = :externalContentId
            """)
    boolean existsExternal(
            @Param("sourceType") SpotSourceType sourceType,
            @Param("externalContentId") String externalContentId);

    @Query(
            """
            SELECT s
            FROM Spot s
            WHERE s.sourceType = :sourceType
              AND s.externalContentId IN :externalContentIds
            """)
    List<Spot> findExternalSpots(
            @Param("sourceType") SpotSourceType sourceType,
            @Param("externalContentIds") Collection<String> externalContentIds);

    @Query(
            """
            SELECT s
            FROM Spot s
            WHERE s.latitude BETWEEN :minLat AND :maxLat
              AND s.longitude BETWEEN :minLng AND :maxLng
            """)
    List<Spot> findMapBounds(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng);

    @Query(
            """
            SELECT s
            FROM Spot s
            WHERE s.sourceType = :sourceType
              AND s.category IN :categories
              AND (s.thumbnailUrl IS NULL OR s.thumbnailUrl = '')
            ORDER BY s.id ASC
            """)
    List<Spot> findThumbnailTargets(
            @Param("sourceType") SpotSourceType sourceType,
            @Param("categories") Collection<SpotCategory> categories,
            Pageable pageable);
}
