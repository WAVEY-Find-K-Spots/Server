package com.Wavey.WaveyService.route.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private Long route_id;
    private Long user_id;
    private String title;
    private String description;
    private Float progress;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}