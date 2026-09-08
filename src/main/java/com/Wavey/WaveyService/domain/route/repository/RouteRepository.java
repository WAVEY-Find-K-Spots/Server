package com.Wavey.WaveyService.domain.route.repository;

import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.Visibility;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Route r where r.id = :id")
    Optional<Route> findLockedById(Long id);

    List<Route> findByUserId(Long userId);

    List<Route> findByUserIdAndVisibility(Long userId, Visibility visibility);

    Page<Route> findByVisibility(Visibility visibility, Pageable pageable);

    Page<Route> findByVisibilityAndRouteSpots_SpotIdIn(
            Visibility visibility, List<Long> spotIds, Pageable pageable);
}
