package com.codit.worldcup.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "worldcup_id", nullable = false)
	private Long worldcupId;

	@Column(name = "winner_id", nullable = false)
	private Long winnerId; // 최종 우승 후보 ID

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	@Builder
	public UserResult(Long userId, Long worldcupId, Long winnerId) {
		this.userId = userId;
		this.worldcupId = worldcupId;
		this.winnerId = winnerId;
	}
}