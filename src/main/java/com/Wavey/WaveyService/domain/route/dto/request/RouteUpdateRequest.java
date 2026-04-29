package com.Wavey.WaveyService.domain.route.dto.request;

import com.Wavey.WaveyService.domain.route.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "루트 수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteUpdateRequest {

    @Schema(description = "변경할 루트 이름", example = "수정된 루트 이름")
    @Size(max = 50)
    private String name;

    @Schema(description = "변경할 설명", example = "업데이트된 설명")
    @Size(max = 512)
    private String description;

    @Schema(description = "변경할 공개 여부", example = "PRIVATE")
    private Visibility visibility;
}