package com.Wavey.WaveyService;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "com.google.cloud.spring.autoconfigure.vision.CloudVisionAutoConfiguration",
        "GCP_PROJECT_ID=test-project",
        "GCP_JSON_CREDENTIALS_BASE64=e30=",
        "GOOGLE_CLIENT_ID=test-client",
        "GOOGLE_CLIENT_SECRET=test-secret",
        "ADMIN_EMAILS=test@example.com",
        "PUBLIC_DATA_SERVICE_KEY=test-key",
        "key=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "a_exp=3600000",
        "r_exp=604800000"
})
class WaveyServiceApplicationTests {

    @MockitoBean
    private CloudVisionTemplate cloudVisionTemplate;

    @MockitoBean
    private ImageAnnotatorClient imageAnnotatorClient;

    @MockitoBean
    private CredentialsProvider credentialsProvider;

    @Test
    void contextLoads() {
    }
}