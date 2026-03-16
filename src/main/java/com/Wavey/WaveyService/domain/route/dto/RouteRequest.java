package com.Wavey.WaveyService.domain.route.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    private Long user_id;
    private String title;
    private String description;
    private Float progress;
}
