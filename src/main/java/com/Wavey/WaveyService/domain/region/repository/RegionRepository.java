package com.Wavey.WaveyService.domain.region.repository;

import com.Wavey.WaveyService.domain.region.entity.Region;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findAllByOrderByIdAsc();
    Optional<Region> findByNameKo(String nameKo);
}