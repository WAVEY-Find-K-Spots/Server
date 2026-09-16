package com.Wavey.WaveyService.domain.policy.repository;

import com.Wavey.WaveyService.domain.policy.entity.PolicyDocument;
import com.Wavey.WaveyService.domain.policy.enums.PolicyCategory;
import com.Wavey.WaveyService.domain.user.enums.Language;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDocumentRepository extends JpaRepository<PolicyDocument, Long> {

    Optional<PolicyDocument> findByCategoryAndLanguageAndActiveTrue(
            PolicyCategory category,
            Language language
    );
}
