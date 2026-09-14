package com.Wavey.WaveyService.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        tags = {
                @Tag(name = "Contents", description = "콘텐츠 CRUD, 영상/앨범/트랙 조회·수집·숨김"),
                @Tag(name = "Spot", description = "스팟(장소) 관리 API"),
                @Tag(name = "Spot Sync", description = "외부 공공데이터 장소 동기화 API"),
                @Tag(name = "Region", description = "지역 관리 API"),
                @Tag(name = "Route", description = "루트 관리 API"),
                @Tag(name = "RouteSpot", description = "루트 스팟 관리 API"),
                @Tag(name = "RouteDirections", description = "루트 경로 계산 API"),
                @Tag(name = "User Auth", description = "인증 및 회원 관리 API"),
                @Tag(name = "Vision", description = "Google Cloud Vision 기반 이미지 분석 API")
        }
)
public class SwaggerConfig {

    private static final List<String> TAG_ORDER = List.of(
            "Contents",
            "Spot",
            "Spot Sync",
            "Region",
            "Route",
            "RouteSpot",
            "RouteDirections",
            "User Auth",
            "Vision"
    );

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    /** 컨트롤러 스캔 이후에도 태그 순서를 유지한다. */
    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            Map<String, io.swagger.v3.oas.models.tags.Tag> byName = new LinkedHashMap<>();
            if (openApi.getTags() != null) {
                for (io.swagger.v3.oas.models.tags.Tag tag : openApi.getTags()) {
                    byName.put(tag.getName(), tag);
                }
            }
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(path -> path.readOperations().forEach(op -> {
                    if (op.getTags() == null) {
                        return;
                    }
                    for (String name : op.getTags()) {
                        byName.putIfAbsent(name, new io.swagger.v3.oas.models.tags.Tag().name(name));
                    }
                }));
            }

            List<io.swagger.v3.oas.models.tags.Tag> ordered = new ArrayList<>();
            for (String name : TAG_ORDER) {
                io.swagger.v3.oas.models.tags.Tag tag = byName.remove(name);
                if (tag != null) {
                    ordered.add(tag);
                }
            }
            ordered.addAll(byName.values());
            openApi.setTags(ordered);
        };
    }
}
