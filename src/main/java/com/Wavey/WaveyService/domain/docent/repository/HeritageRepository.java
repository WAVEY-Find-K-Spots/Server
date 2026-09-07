package com.Wavey.WaveyService.domain.docent.repository;

import com.Wavey.WaveyService.domain.docent.model.HeritageRecord;
import java.util.List;

public interface HeritageRepository {
    List<HeritageRecord> findCandidates(
            String rawText, List<String> visionCandidates, String region1, String region2
    );
}
