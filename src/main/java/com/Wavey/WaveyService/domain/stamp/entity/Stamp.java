package com.Wavey.WaveyService.domain.stamp.entity;

import com.Wavey.WaveyService.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "stamp",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stamp_spot_category",
                        columnNames = {"spot_id", "category"}
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stamp extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stamp_id")
    private Long stampId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "img_link", nullable = false, length = 500)
    private String imgLink;

    @Convert(converter = StampCategoryConverter.class)
    @Column(nullable = false, length = 20)
    private StampCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String context;

    @Column(columnDefinition = "TEXT")
    private String requirement;

    public void update(Long spotId, String imgLink, StampCategory category, String name, String context, String requirement) {
        this.spotId = spotId;
        this.imgLink = imgLink;
        this.category = category;
        this.name = name;
        this.context = context;
        this.requirement = requirement;
    }
}
