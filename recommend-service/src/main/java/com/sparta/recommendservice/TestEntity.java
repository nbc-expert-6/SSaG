package com.sparta.recommendservice;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(schema = "recommend_service_db")
public class TestEntity {
	@Id
	private Long id;

	private String name;
}
