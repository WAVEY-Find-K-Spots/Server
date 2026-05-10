package com.Wavey.WaveyService.domain.stamp.controller;

import com.Wavey.WaveyService.domain.stamp.dto.StampRequest;
import com.Wavey.WaveyService.domain.stamp.dto.StampResponse;
import com.Wavey.WaveyService.domain.stamp.service.StampService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Stamps", description = "스탬프 원본 마스터 데이터 API")
@RestController
@RequestMapping("/api/stamps")
@RequiredArgsConstructor
public class StampController {

    private final StampService stampService;

    @Operation(summary = "스탬프 원본 등록")
    @PostMapping
    public ResponseEntity<CommonResponse<StampResponse>> createStamp(@Valid @RequestBody StampRequest request) {
        return ResponseEntity.ok(CommonResponse.success("스탬프 원본 등록 성공", stampService.createStamp(request)));
    }

    @Operation(summary = "스탬프 원본 전체 조회")
    @GetMapping
    public ResponseEntity<CommonResponse<List<StampResponse>>> getAllStamps() {
        return ResponseEntity.ok(CommonResponse.success("스탬프 원본 전체 조회 성공", stampService.getAllStamps()));
    }

    @Operation(summary = "스탬프 원본 상세 조회")
    @GetMapping("/{stampId}")
    public ResponseEntity<CommonResponse<StampResponse>> getStamp(@PathVariable Long stampId) {
        return ResponseEntity.ok(CommonResponse.success("스탬프 원본 상세 조회 성공", stampService.getStampById(stampId)));
    }

    @Operation(summary = "스탬프 원본 수정")
    @PutMapping("/{stampId}")
    public ResponseEntity<CommonResponse<StampResponse>> updateStamp(@PathVariable Long stampId,
                                                                     @Valid @RequestBody StampRequest request) {
        return ResponseEntity.ok(CommonResponse.success("스탬프 원본 수정 성공", stampService.updateStamp(stampId, request)));
    }

    @Operation(summary = "스탬프 원본 삭제")
    @DeleteMapping("/{stampId}")
    public ResponseEntity<CommonResponse<Void>> deleteStamp(@PathVariable Long stampId) {
        stampService.deleteStamp(stampId);
        return ResponseEntity.ok(CommonResponse.success("스탬프 원본 삭제 성공", null));
    }
}
