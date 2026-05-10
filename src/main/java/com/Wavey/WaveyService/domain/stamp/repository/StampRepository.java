package com.Wavey.WaveyService.domain.stamp.repository;

import com.Wavey.WaveyService.domain.stamp.entity.Stamp;
import com.Wavey.WaveyService.domain.stamp.entity.StampCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StampRepository extends JpaRepository<Stamp, Long> {

    Optional<Stamp> findBySpotIdAndCategory(Long spotId, StampCategory category);
}
