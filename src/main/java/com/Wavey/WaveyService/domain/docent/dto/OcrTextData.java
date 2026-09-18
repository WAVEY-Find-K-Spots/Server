package com.Wavey.WaveyService.domain.docent.dto;

import java.util.List;

public record OcrTextData(
        String text,
        List<OcrTextBlock> blocks
) {
    public OcrTextData {
        text = text == null ? "" : text;
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
    }

    public static OcrTextData empty() {
        return new OcrTextData("", List.of());
    }
}
