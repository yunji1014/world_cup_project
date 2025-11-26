package com.codit.worldcup.presentation.controller;

import com.codit.worldcup.application.service.WorldcupService;
import com.codit.worldcup.presentation.dto.CommentRequest;
import com.codit.worldcup.presentation.dto.CommentResponse;
import com.codit.worldcup.presentation.dto.SelectionRequest;
import com.codit.worldcup.presentation.dto.WorldcupDetailResponse;
import com.codit.worldcup.presentation.dto.WorldcupListResponse;
import com.codit.worldcup.presentation.dto.WorldcupResultResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WorldcupController {

	private final WorldcupService worldcupService;

	public WorldcupController(WorldcupService worldcupService) {
		this.worldcupService = worldcupService;
	}

	// 월드컵 목록 조회 (GET /api/main)
	@GetMapping("/main")
	public List<WorldcupListResponse> getWorldcupList() {
		return worldcupService.findAllWorldcups();
	}

	@GetMapping("/worldcup/{worldcupId}")
	public WorldcupDetailResponse getWorldcupDetail(@PathVariable Long worldcupId) {
		return worldcupService.findWorldcupDetail(worldcupId);
	}

	@PostMapping("/worldcup/{worldcupId}/select")
	@ResponseStatus(HttpStatus.NO_CONTENT) // 💡 204 No Content를 명시적으로 지정
	public void recordSelection(@PathVariable Long worldcupId, @RequestBody SelectionRequest request) {

		// winnerId와 loserId, 그리고 현재 라운드를 받습니다.
		if (request.getWinnerId() == null || request.getLoserId() == null) {
			throw new IllegalArgumentException("선택된 후보 정보가 필요합니다.");
		}

		worldcupService.recordSelection(worldcupId, request.getWinnerId(), request.getLoserId(), request.getRound(), request.getUserId());

		// 응답 본문 없이 200 OK만 반환합니다.
	}

	/**
	 * GET /api/result/{worldcupId}/{winnerId} : 특정 월드컵의 최종 결과 정보를 조회합니다.
	 * 💡 winnerId를 PathVariable로 받도록 수정
	 */
	@GetMapping("/result/{worldcupId}/{winnerId}")
	public WorldcupResultResponse getFinalResult(@PathVariable Long worldcupId, @PathVariable Long winnerId) {
		return worldcupService.findFinalResult(worldcupId, winnerId);
	}

	/**
	 * GET /api/result/{id}/comments : 특정 월드컵의 댓글 목록을 조회합니다.
	 */
	@GetMapping("/result/{worldcupId}/comments")
	public List<CommentResponse> getComments(@PathVariable Long worldcupId) {
		return worldcupService.findCommentsByWorldcup(worldcupId);
	}

	/**
	 * POST /api/result/{id}/comments : 댓글을 작성합니다.
	 */
	@PostMapping("/result/{worldcupId}/comments")
	@ResponseStatus(HttpStatus.NO_CONTENT) // 201 Created 응답
	public void addComment(@PathVariable Long worldcupId, @RequestBody CommentRequest request) {
		// 💡 주의: 현재 userId는 임시값(1L)을 사용합니다. 실제는 세션에서 가져와야 합니다.
		Long dummyUserId = 1L;
		worldcupService.addComment(worldcupId, request.getUserId(), request.getContent());
	}
}