package com.codit.worldcup.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {
	private Long userId; // 사용자 식별을 위해 ID도 포함
	private String nickname;
}