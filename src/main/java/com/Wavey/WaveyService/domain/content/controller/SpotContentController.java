package com.Wavey.WaveyService.domain.content.controller;

import com.Wavey.WaveyService.domain.content.dto.SpotContentItemResponse;
import com.Wavey.WaveyService.domain.content.dto.SpotMediaResponse;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.service.ContentMediaQueryService;
import com.Wavey.WaveyService.domain.content.service.SpotContentService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Contents", description = "콘텐츠 CRUD, 영상/앨범/트랙 조회·수집·숨김")
@RestController
@RequiredArgsConstructor
public class SpotContentController {

    private final SpotContentService spotContentService;
    private final ContentMediaQueryService contentMediaQueryService;

    @Operation(
            summary = "스팟 연결 콘텐츠 조회",
            description = "스팟에 연결된 작품 목록. category 없으면 전체, 있으면 해당 카테고리만."
    )
    @GetMapping("/api/v1/spots/{spotId}/contents")
    public ResponseEntity<CommonResponse<List<SpotContentItemResponse>>> list(
            @PathVariable Long spotId,
            @Parameter(description = "ARTIST | DRAMA | MOVIE") @RequestParam(required = false) ContentCategory category
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "스팟 연결 콘텐츠 조회 성공",
                spotContentService.list(spotId, category)));
    }

    @Operation(
            summary = "스팟 연결 콘텐츠 미디어 카드 조회",
            description = "스팟에 연결된 모든 콘텐츠의 유튜브 영상/스포티파이 앨범·트랙 카드를 콘텐츠별로 묶어서 반환합니다."
    )
    @GetMapping("/api/v1/spots/{spotId}/media")
    public ResponseEntity<CommonResponse<SpotMediaResponse>> media(
            @PathVariable Long spotId
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "스팟 연결 콘텐츠 미디어 조회 성공",
                contentMediaQueryService.getSpotMedia(spotId)));
    }
}
