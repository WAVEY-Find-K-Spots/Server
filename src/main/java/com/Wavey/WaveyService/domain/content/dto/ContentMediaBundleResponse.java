package com.Wavey.WaveyService.domain.content.dto;

import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentMediaBundleResponse {

    private Long contentId;
    private String title;
    private ContentCategory category;

    @Schema(description = "YouTube 영상 카드")
    private List<ContentVideoResponse> videos;

    @Schema(description = "앨범 카드(수록 트랙 포함). 없으면 빈 배열")
    private List<ContentAlbumResponse> albums;

    @Schema(description = "앨범에 속하지 않는 단독 트랙. 없으면 빈 배열")
    private List<ContentTrackResponse> tracks;
}
