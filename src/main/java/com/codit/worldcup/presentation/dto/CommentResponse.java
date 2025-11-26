package com.codit.worldcup.presentation.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
public class CommentResponse {
	private Long id;
	private String nickname;
	private String content;
	private LocalDateTime createdAt;
}