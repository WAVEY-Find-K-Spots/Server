package com.Wavey.WaveyService.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    @Schema(description = "루트 ID", example = "1")
    private Long route_id;

    @Schema(description = "사용자 ID", example = "105")
    private Long user_id;

    @Schema(description = "루트 제목", example = "여의도 한강 공원 산책 코스")
    private String title;

    @Schema(description = "루트 설명", example = "한강을 따라 걷는 여유로운 산책길입니다.")
    private String description;

    @Schema(description = "진행률", example = "0.5")
    private Float progress;

    @Schema(description = "생성 시간", example = "2026-03-18T10:00:00")
    private LocalDateTime created_at;

    @Schema(description = "수정 시간", example = "2026-03-18T11:00:00")
    private LocalDateTime updated_at;

}
