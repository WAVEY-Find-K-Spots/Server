package com.Wavey.WaveyService.domain.spot.repository;

import com.Wavey.WaveyService.domain.spot.entity.SavedSpot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SavedSpotRepository extends JpaRepository<SavedSpot, Long> {

    boolean existsByUserIdAndSpotId(Long userId, Long spotId);

    Optional<SavedSpot> findByUserIdAndSpotId(Long userId, Long spotId);

    void deleteByUserIdAndSpotId(Long userId, Long spotId);

    @Query("select s.spotId from SavedSpot s where s.userId = :userId and s.spotId in :spotIds")
    List<Long> findSpotIdsByUserIdAndSpotIdIn(@Param("userId") Long userId, @Param("spotIds") Set<Long> spotIds);

    @Query("select s.spotId from SavedSpot s where s.userId = :userId")
    List<Long> findSpotIdsByUserId(@Param("userId") Long userId);

    @Query(
            "select distinct s.userId from SavedSpot s "
                    + "where s.spotId = :spotId and s.userId <> :excludedUserId")
    List<Long> findUserIdsBySpotIdExcludingUser(
            @Param("spotId") Long spotId, @Param("excludedUserId") Long excludedUserId);
}
