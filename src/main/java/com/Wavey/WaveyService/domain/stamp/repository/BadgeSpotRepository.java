package com.Wavey.WaveyService.domain.stamp.repository;

import com.Wavey.WaveyService.domain.stamp.entity.BadgeSpot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BadgeSpotRepository extends JpaRepository<BadgeSpot, Long> {

    List<BadgeSpot> findByBadgeId(Long badgeId);

    List<BadgeSpot> findByBadgeIdIn(Collection<Long> badgeIds);

    @Query("select bs.spotId from BadgeSpot bs where bs.badgeId = :badgeId")
    List<Long> findSpotIdsByBadgeId(@Param("badgeId") Long badgeId);

    void deleteByBadgeId(Long badgeId);

    void deleteBySpotId(Long spotId);

    boolean existsByBadgeId(Long badgeId);
}
