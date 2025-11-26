package com.codit.worldcup.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "worldcup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Worldcup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "thumbnail_url", length = 512)
	private String thumbnailUrl;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	// 월드컵 생성 시 사용될 Builder
	@Builder
	public Worldcup(String title, String thumbnailUrl) {
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
	}
	public void update(String title, String thumbnailUrl) {
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
	}

}