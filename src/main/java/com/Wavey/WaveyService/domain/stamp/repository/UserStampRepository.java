package com.Wavey.WaveyService.domain.stamp.repository;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.stamp.entity.UserStamp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserStampRepository extends JpaRepository<UserStamp, Long> {

    Optional<UserStamp> findByUserIdAndStampId(Long userId, Long stampId);

    Optional<UserStamp> findByUserIdAndSpotId(Long userId, Long spotId);

    List<UserStamp> findByUserId(Long userId);

    long countByUserId(Long userId);

    void deleteBySpotId(Long spotId);

    /** 스탬프북용: 내가 획득한 스팟을 최근 획득 순으로 먼저, 이어서 미획득 스팟을 id 순으로 조회한다. */
    @Query(
            value =
                    """
                    select s from Spot s
                    left join UserStamp us on us.spotId = s.id and us.userId = :userId
                    order by case when us.id is null then 1 else 0 end, us.acquiredAt desc, s.id asc
                    """,
            countQuery = "select count(s) from Spot s")
    Page<Spot> findSpotsAcquiredFirst(@Param("userId") Long userId, Pageable pageable);

    @Query(
            value =
                    """
                    select s from Spot s
                    left join UserStamp us on us.spotId = s.id and us.userId = :userId
                    where s.regionId = :regionId
                    order by case when us.id is null then 1 else 0 end, us.acquiredAt desc, s.id asc
                    """,
            countQuery = "select count(s) from Spot s where s.regionId = :regionId")
    Page<Spot> findSpotsByRegionAcquiredFirst(
            @Param("userId") Long userId,
            @Param("regionId") Long regionId,
            Pageable pageable);
}
