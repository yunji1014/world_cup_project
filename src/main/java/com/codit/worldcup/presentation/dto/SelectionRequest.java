package com.codit.worldcup.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SelectionRequest {
	private Long winnerId; // 사용자가 선택한 후보 ID
	private Long loserId;  // 사용자가 선택하지 않은 후보 ID
	private int round;     // 현재 라운드 (프론트엔드에서 계산되어 넘어옴)
	private Long userId;
}