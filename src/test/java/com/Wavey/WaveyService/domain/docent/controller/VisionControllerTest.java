package com.Wavey.WaveyService.domain.docent.controller;

import com.Wavey.WaveyService.domain.docent.dto.VisionFeature;
import com.Wavey.WaveyService.domain.docent.service.VisionRequestRateLimiter;
import com.Wavey.WaveyService.domain.docent.service.VisionService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VisionControllerTest {

    @Mock
    private VisionService visionService;
    @Mock
    private VisionRequestRateLimiter requestRateLimiter;

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
                        new MockHttpServletRequest(),
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
                        new MockHttpServletRequest(),
                        file,
                        Set.of(VisionFeature.HERITAGE),
                        37.579617,
                        126.977041
                ),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
    }

    @Test
    void PNG로_위장한_텍스트는_외부_API_호출_전에_거부한다() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                "not an image".getBytes(StandardCharsets.UTF_8)
        );

        CustomException exception = catchThrowableOfType(
                () -> visionController.analyze(
                        null,
                        new MockHttpServletRequest(),
                        file,
                        Set.of(VisionFeature.TRANSLATION),
                        null,
                        null
                ),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
    }

    @Test
    void 실제_PNG와_파라미터가_있는_MIME_타입은_허용한다() {
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pixel.png",
                "image/png; charset=UTF-8",
                png
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        visionController.analyze(
                null,
                request,
                file,
                Set.of(VisionFeature.TRANSLATION),
                null,
                null
        );

        verify(requestRateLimiter).check(null, "127.0.0.1");
        verify(visionService).analyze(
                any(),
                eq(Set.of(VisionFeature.TRANSLATION)),
                eq(null),
                eq(null)
        );
    }
}
