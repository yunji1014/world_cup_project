package com.codit.worldcup.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "worldcup_id", nullable = false)
	private Long worldcupId;

	@Column(name = "user_id", nullable = false)
	private Long userId; // 작성자 ID

	@Column(name = "content", nullable = false, length = 500)
	private String content;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	@Builder
	public Comment(Long worldcupId, Long userId, String content) {
		this.worldcupId = worldcupId;
		this.userId = userId;
		this.content = content;
	}
}