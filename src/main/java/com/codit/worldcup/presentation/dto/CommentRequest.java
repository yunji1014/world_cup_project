package com.codit.worldcup.presentation.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
public class CommentRequest {
	private String content; // 댓글 내용
	private Long userId;
	private String nickname;
}