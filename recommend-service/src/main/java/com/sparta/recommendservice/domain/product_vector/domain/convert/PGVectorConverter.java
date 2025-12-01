package com.sparta.recommendservice.domain.product_vector.domain.convert;

import com.pgvector.PGvector;

import jakarta.persistence.AttributeConverter;

public class PGVectorConverter implements AttributeConverter<PGvector, String> {

	@Override
	public String convertToDatabaseColumn(PGvector attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.toString();
	}

	@Override
	public PGvector convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}

		dbData = dbData.replaceAll("[\\[\\]]", "");

		String[] parts = dbData.split(",");
		float[] values = new float[parts.length];
		for (int i = 0; i < parts.length; i++) {
			values[i] = Float.parseFloat(parts[i]);
		}

		return new PGvector(values);
	}

}
