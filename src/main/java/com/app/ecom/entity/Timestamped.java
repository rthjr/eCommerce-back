package com.app.ecom.entity;

import java.time.LocalDateTime;

public interface Timestamped {
	LocalDateTime getCreatedAt();
	LocalDateTime getUpdatedAt();
}
