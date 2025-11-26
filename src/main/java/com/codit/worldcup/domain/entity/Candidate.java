package com.codit.worldcup.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Candidate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "worldcup_id", nullable = false)
	private Long worldcupId; // FK (단방향 매핑 대신 ID로만 관리)

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "image_url", length = 512)
	private String imageUrl;

	@Column(name = "win_count", nullable = false)
	private int winCount = 0; // 1등으로 뽑힌 횟수

	@Column(name = "total_selection_count", nullable = false)
	private int totalSelectionCount = 0; // 총 선택된 횟수

	@Builder
	public Candidate(Long worldcupId, String name, String imageUrl) {
		this.worldcupId = worldcupId;
		this.name = name;
		this.imageUrl = imageUrl;
		this.winCount = 0;
		this.totalSelectionCount = 0;
	}

	// 랭킹 집계를 위한 메서드
	public void incrementWinCount() {
		this.winCount += 1;
	}

	public void incrementTotalSelectionCount() {
		this.totalSelectionCount += 1;
	}
	public void update(String name, String imageUrl) {
		this.name = name;
		this.imageUrl = imageUrl;
	}

}