package com.Wavey.WaveyService.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CountryCode {

    KR("대한민국", "South Korea"),
    CN("중국", "China"),
    VN("베트남", "Vietnam"),
    TH("태국", "Thailand"),
    UZ("우즈베키스탄", "Uzbekistan"),
    NP("네팔", "Nepal"),
    KH("캄보디아", "Cambodia"),
    ID("인도네시아", "Indonesia"),
    PH("필리핀", "Philippines"),
    MM("미얀마", "Myanmar"),
    MN("몽골", "Mongolia"),
    US("미국", "United States"),
    KZ("카자흐스탄", "Kazakhstan"),
    LK("스리랑카", "Sri Lanka"),
    RU("러시아", "Russia"),
    BD("방글라데시", "Bangladesh");

    private final String nameKo;
    private final String nameEn;

    public String getDisplayName(String language) {
        return "en".equalsIgnoreCase(language) ? nameEn : nameKo;
    }
}