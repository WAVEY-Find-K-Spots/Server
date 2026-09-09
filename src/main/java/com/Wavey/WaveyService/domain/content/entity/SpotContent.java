package com.Wavey.WaveyService.domain.content.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spot_contents", uniqueConstraints = @UniqueConstraint(
        name = "uk_spot_contents_spot_content", columnNames = {"spot_id", "content_id"}))
@AttributeOverride(name = "id", column = @Column(name = "id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotContent extends BaseEntity {
    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;
}
