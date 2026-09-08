package com.Wavey.WaveyService.domain.docent.repository;

import com.Wavey.WaveyService.domain.docent.model.CulturalTerm;

import java.util.List;

/**
 * 번역 서비스가 한국어기초사전 데이터의 저장 형식에 의존하지 않도록 하는 내부 경계입니다.
 * 테스트에서는 이 인터페이스에 raw 용어 데이터를 전달해 실제 데이터셋 없이 검증합니다.
 */
public interface CulturalTermRepository {

    List<CulturalTerm> findCandidates(String text);
}
