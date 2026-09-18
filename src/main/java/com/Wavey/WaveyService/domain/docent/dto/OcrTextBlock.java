package com.Wavey.WaveyService.domain.docent.dto;

import java.util.List;

public record OcrTextBlock(
        String text,
        List<OcrPoint> polygon,
        float confidence
) {
    public OcrTextBlock {
        text = text == null ? "" : text;
        polygon = polygon == null ? List.of() : List.copyOf(polygon);
    }
}
