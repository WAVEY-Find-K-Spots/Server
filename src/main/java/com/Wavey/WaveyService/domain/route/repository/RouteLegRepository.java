package com.Wavey.WaveyService.domain.route.repository;

import com.Wavey.WaveyService.domain.route.entity.RouteLeg;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface RouteLegRepository extends JpaRepository<RouteLeg, Long> {
    Optional<RouteLeg> findByFromSpotIdAndToSpotIdAndMode(
            Long fromSpotId, Long toSpotId, RouteLeg.TravelMode mode);
}
