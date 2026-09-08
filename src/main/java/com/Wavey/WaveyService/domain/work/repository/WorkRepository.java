package com.Wavey.WaveyService.domain.work.repository;

import com.Wavey.WaveyService.domain.work.entity.Work;
import com.Wavey.WaveyService.domain.work.entity.WorkType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Long> {

    Optional<Work> findByTitleAndType(String title, WorkType type);

    List<Work> findByTypeOrderByIdDesc(WorkType type);

    List<Work> findAllByOrderByIdDesc();
}
