package com.Wavey.WaveyService.domain.review.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_user_spot",
                        columnNames = {"user_id", "spot_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_review_spot_created",
                        columnList = "spot_id, created_at"
                ),
                @Index(
                        name = "idx_review_user_created",
                        columnList = "user_id, created_at"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false, length = 2000)
    private String body;

    public void update(
            Double rating,
            String body
    ) {
        if (rating != null) {
            this.rating = rating;
        }

        if (body != null) {
            this.body = body.strip();
        }
    }
}
