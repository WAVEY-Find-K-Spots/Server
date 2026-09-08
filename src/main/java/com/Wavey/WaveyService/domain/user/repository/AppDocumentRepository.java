package com.Wavey.WaveyService.domain.user.repository;

import com.Wavey.WaveyService.domain.user.entity.AppDocument;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.*;

public interface AppDocumentRepository extends JpaRepository<AppDocument, Long> {
    Optional<AppDocument>
            findFirstByTypeAndLanguageAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
                    String type, String language, LocalDateTime now);
}
