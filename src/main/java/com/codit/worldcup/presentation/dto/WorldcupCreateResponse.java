package com.codit.worldcup.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorldcupCreateResponse {
	private Long worldcupId;
	private String message;
}