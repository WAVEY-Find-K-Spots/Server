package com.Wavey.WaveyService.domain.docent.controller;

import com.Wavey.WaveyService.domain.docent.dto.VisionFeature;
import com.Wavey.WaveyService.domain.docent.service.VisionService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@ExtendWith(MockitoExtension.class)
class VisionControllerTest {

    @Mock
    private VisionService visionService;

    @InjectMocks
    private VisionController visionController;

    @Test
    void 이미지가_아닌_파일은_이미지_타입_오류를_반환한다() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "경복궁".getBytes(StandardCharsets.UTF_8)
        );

        CustomException exception = catchThrowableOfType(
                () -> visionController.analyze(
                        null,
                        file,
                        Set.of(VisionFeature.TRANSLATION),
                        null,
                        null
                ),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
        assertThat(exception.getErrorCode().getCode()).isEqualTo("VISION_400_IMAGE_TYPE");
    }

    @Test
    void 지원하지_않는_이미지_형식은_이미지_타입_오류를_반환한다() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.gif",
                "image/gif",
                new byte[]{1, 2, 3}
        );

        CustomException exception = catchThrowableOfType(
                () -> visionController.analyze(
                        null,
                        file,
                        Set.of(VisionFeature.HERITAGE),
                        37.579617,
                        126.977041
                ),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
    }
}