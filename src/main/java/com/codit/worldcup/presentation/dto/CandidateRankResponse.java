package com.codit.worldcup.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class CandidateRankResponse {
	private Long id;
	private String name;
	private int winCount;
	private int totalSelectionCount;
	private List<String> topWinnerNicknames;
}