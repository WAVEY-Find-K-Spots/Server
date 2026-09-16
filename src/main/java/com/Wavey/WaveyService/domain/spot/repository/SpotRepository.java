package com.Wavey.WaveyService.domain.spot.repository;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.ExternalSource;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long>, JpaSpecificationExecutor<Spot> {

    Page<Spot> findByRegionId(Long regionId, Pageable pageable);

    Optional<Spot> findFirstByNameKoAndAddressKoAndCategory(String nameKo, String addressKo, SpotCategory category);

    Optional<Spot> findByExternalSourceAndExternalId(ExternalSource externalSource, String externalId);

    @Query("select s from Spot s where s.imageUrl is null or s.imageUrl = '' order by s.id asc")
    List<Spot> findMissingImage(Pageable pageable);

    @Query("select s.id from Spot s where s.regionId = :regionId")
    List<Long> findIdsByRegionId(@Param("regionId") Long regionId);

    @Query(
            value = """
                    SELECT s.*
                    FROM spot s
                    WHERE s.spot_id <> :spotId
                      AND s.latitude BETWEEN :minLat AND :maxLat
                      AND s.longitude BETWEEN :minLng AND :maxLng
                      AND (
                          6371000.0 * 2.0 *
                          ASIN(
                              SQRT(
                                  LEAST(
                                      1.0,
                                      GREATEST(
                                          0.0,
                                          POWER(
                                              SIN(
                                                  RADIANS(CAST(s.latitude AS double precision) - :latitude) / 2.0
                                              ),
                                              2
                                          )
                                          +
                                          COS(RADIANS(:latitude))
                                          *
                                          COS(RADIANS(CAST(s.latitude AS double precision)))
                                          *
                                          POWER(
                                              SIN(
                                                  RADIANS(CAST(s.longitude AS double precision) - :longitude) / 2.0
                                              ),
                                              2
                                          )
                                      )
                                  )
                              )
                          )
                      ) <= :radiusMeters
                    ORDER BY (
                        6371000.0 * 2.0 *
                        ASIN(
                            SQRT(
                                LEAST(
                                    1.0,
                                    GREATEST(
                                        0.0,
                                        POWER(
                                            SIN(
                                                RADIANS(CAST(s.latitude AS double precision) - :latitude) / 2.0
                                            ),
                                            2
                                        )
                                        +
                                        COS(RADIANS(:latitude))
                                        *
                                        COS(RADIANS(CAST(s.latitude AS double precision)))
                                        *
                                        POWER(
                                            SIN(
                                                RADIANS(CAST(s.longitude AS double precision) - :longitude) / 2.0
                                            ),
                                            2
                                        )
                                    )
                                )
                            )
                        )
                    ) ASC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<Spot> findNearby(
            @Param("spotId") Long spotId,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng,
            @Param("radiusMeters") double radiusMeters,
            @Param("limit") int limit
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Spot s WHERE s.id = :spotId")
    Optional<Spot> findLockedById(@Param("spotId") Long spotId);
}
