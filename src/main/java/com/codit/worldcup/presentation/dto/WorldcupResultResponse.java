package com.codit.worldcup.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class WorldcupResultResponse {
	private WinnerCandidateDto winner; // 최종 우승 후보 상세 정보
	private List<String> topWinnerNicknames; // 해당 후보를 1등으로 뽑은 사용자 닉네임 목록

	@Getter
	@AllArgsConstructor
	public static class WinnerCandidateDto {
		private Long id;
		private String name;
		private String imagePath; // 프론트엔드 필드명과 통일 (imageUrl -> imagePath)
	}
}