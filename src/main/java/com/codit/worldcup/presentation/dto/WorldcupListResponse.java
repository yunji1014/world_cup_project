package com.codit.worldcup.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorldcupListResponse {
	private Long id;
	private String title;
	private String thumbnailUrl;
}