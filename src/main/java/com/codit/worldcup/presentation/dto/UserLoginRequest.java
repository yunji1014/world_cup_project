package com.codit.worldcup.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLoginRequest {
	private String nickname;
	private String password;
}