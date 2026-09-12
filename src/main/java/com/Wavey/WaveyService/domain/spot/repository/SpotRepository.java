package com.Wavey.WaveyService.domain.spot.repository;

import com.Wavey.WaveyService.domain.spot.entity.Spot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpotRepository
        extends JpaRepository<Spot, Long>,
        JpaSpecificationExecutor<Spot> {

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
                                                  RADIANS(
                                                      CAST(s.latitude AS double precision)
                                                      - :latitude
                                                  ) / 2.0
                                              ),
                                              2
                                          )
                                          +
                                          COS(RADIANS(:latitude))
                                          *
                                          COS(
                                              RADIANS(
                                                  CAST(s.latitude AS double precision)
                                              )
                                          )
                                          *
                                          POWER(
                                              SIN(
                                                  RADIANS(
                                                      CAST(s.longitude AS double precision)
                                                      - :longitude
                                                  ) / 2.0
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
                                                RADIANS(
                                                    CAST(s.latitude AS double precision)
                                                    - :latitude
                                                ) / 2.0
                                            ),
                                            2
                                        )
                                        +
                                        COS(RADIANS(:latitude))
                                        *
                                        COS(
                                            RADIANS(
                                                CAST(s.latitude AS double precision)
                                            )
                                        )
                                        *
                                        POWER(
                                            SIN(
                                                RADIANS(
                                                    CAST(s.longitude AS double precision)
                                                    - :longitude
                                                ) / 2.0
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
}