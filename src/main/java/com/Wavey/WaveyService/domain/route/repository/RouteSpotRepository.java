package com.Wavey.WaveyService.domain.route.repository;

import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteSpotRepository extends JpaRepository<RouteSpot, Long> {

    List<RouteSpot> findByRouteIdOrderBySequenceOrderAsc(Long routeId);

    boolean existsByRouteIdAndSpotId(Long routeId, Long spotId);

    void deleteByRouteIdAndId(Long routeId, Long routeSpotId);
}
