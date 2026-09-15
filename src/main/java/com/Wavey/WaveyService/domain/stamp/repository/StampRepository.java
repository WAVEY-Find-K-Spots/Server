package com.Wavey.WaveyService.domain.stamp.repository;

import com.Wavey.WaveyService.domain.stamp.entity.Stamp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StampRepository extends JpaRepository<Stamp, Long> {
    Optional<Stamp> findBySpotId(Long spotId);

    List<Stamp> findBySpotIdIn(Collection<Long> spotIds);

    void deleteBySpotId(Long spotId);
}
