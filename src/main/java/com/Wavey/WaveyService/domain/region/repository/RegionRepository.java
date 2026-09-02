package com.Wavey.WaveyService.domain.region.repository;

import com.Wavey.WaveyService.domain.region.entity.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByName(String name);

    Optional<Region> findByCode(String code);

    @Query("""
            SELECT r
            FROM Region r
            ORDER BY r.name ASC
            """)
    List<Region> findAllOrderByName();

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Region r
            WHERE r.code = :code
            """)
    boolean existsCode(@Param("code") String code);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Region r
            WHERE r.code = :code
              AND r.id <> :regionId
            """)
    boolean existsCodeExceptId(
            @Param("code") String code,
            @Param("regionId") Long regionId
    );
}
