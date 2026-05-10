package com.Wavey.WaveyService.domain.stamp.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StampCategoryConverter implements AttributeConverter<StampCategory, String> {

    @Override
    public String convertToDatabaseColumn(StampCategory attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StampCategory convertToEntityAttribute(String dbData) {
        return dbData == null ? null : StampCategory.from(dbData);
    }
}
