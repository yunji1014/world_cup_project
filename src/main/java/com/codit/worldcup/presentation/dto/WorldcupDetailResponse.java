package com.codit.worldcup.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class WorldcupDetailResponse {
	private Long id;
	private String title;
	private String thumbnailUrl;
	private List<CandidateDto> candidates; // 후보 목록

	@Getter
	@AllArgsConstructor
	public static class CandidateDto {
		private Long id;
		private String name;
		private String imagePath; // 프론트엔드 필드명과 통일 (imageUrl -> imagePath)
	}
}