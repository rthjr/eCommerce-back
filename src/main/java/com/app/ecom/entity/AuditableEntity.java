package com.app.ecom.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;

public abstract	class AuditableEntity implements Timestamped{
	
	@CreationTimestamp
	@Column(name = "CREATED_AT", updatable = false)
	private LocalDateTime createdAt;
	
	@CreationTimestamp
	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;
	
	
	
	@Override
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	@Override
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
