package com.Wavey.WaveyService.domain.route.repository;

import com.Wavey.WaveyService.domain.route.entity.RouteNavigation;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface RouteNavigationRepository extends JpaRepository<RouteNavigation, Long> {
    Optional<RouteNavigation> findByRouteId(Long routeId);
}
