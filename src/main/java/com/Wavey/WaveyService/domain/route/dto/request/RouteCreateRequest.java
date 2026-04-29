package com.Wavey.WaveyService.domain.route.dto.request;

import com.Wavey.WaveyService.domain.route.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "루트 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteCreateRequest {

    @Schema(description = "루트 이름", example = "서울 야경 루트", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50)
    private String name;

    @Schema(description = "루트 설명", example = "한강과 남산을 잇는 야경 코스")
    @Size(max = 512)
    private String description;

    @Schema(description = "공개 여부", example = "PUBLIC", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Visibility visibility;

    @Schema(description = "초기 스팟 목록 (생략 시 빈 루트 생성)")
    private List<RouteSpotRequest> spots = new ArrayList<>();
}
