package com.Wavey.WaveyService.domain.route.repository;

import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByUserId(Long userId);

    List<Route> findByUserIdAndVisibility(Long userId, Visibility visibility);

    Page<Route> findByVisibility(Visibility visibility, Pageable pageable);

    Page<Route> findByVisibilityAndRouteSpots_SpotIdIn(Visibility visibility, List<Long> spotIds, Pageable pageable);
}