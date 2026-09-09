package com.Wavey.WaveyService.domain.stamp.repository;

import com.Wavey.WaveyService.domain.stamp.entity.Stamp;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface StampRepository extends JpaRepository<Stamp, Long> {
    Optional<Stamp> findBySpotId(Long spotId);
}
