package com.tibil.BecknBPP.model;

import java.time.OffsetDateTime;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseModel {
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public BaseModel() {
		super();
	}

	public BaseModel(OffsetDateTime createdAt, OffsetDateTime updatedAt) {
		super();
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}
