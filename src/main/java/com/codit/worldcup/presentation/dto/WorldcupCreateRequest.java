package com.codit.worldcup.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class WorldcupCreateRequest {
	private String title;
	private String thumbnailUrl; // 썸네일
	private List<CandidateDto> candidates; // 후보 목록

	// 내부 DTO: 후보 하나당 필요한 정보
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CandidateDto {
		private String id;
		private String name;      // 후보 이름
		private String imagePath; // 프론트와 맞춤 (imagePath)
	}
}