package com.codit.worldcup.presentation.controller;

import java.util.List;

import com.codit.worldcup.application.service.WorldcupService;
import com.codit.worldcup.presentation.dto.CandidateRankResponse;
import com.codit.worldcup.presentation.dto.WorldcupCreateRequest;
import com.codit.worldcup.presentation.dto.WorldcupCreateResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.codit.worldcup.presentation.dto.WorldcupDetailResponse;

@RestController
@RequestMapping("/api")
public class AdminController {

	private final WorldcupService worldcupService;

	public AdminController(WorldcupService worldcupService) {
		this.worldcupService = worldcupService;
	}

	/**
	 * POST /api/main/create : 새로운 월드컵과 후보 목록을 생성합니다. (Admin 전용)
	 */
	@PostMapping("/main/create")
	@ResponseStatus(HttpStatus.CREATED)
	public WorldcupCreateResponse createWorldcup(
		@RequestPart(value = "title") String title,
		@RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,
		@RequestPart(value = "candidatesDataJson") String candidatesDataJson,
		@RequestPart(value = "candidateFiles") List<MultipartFile> candidateFiles
	) {
		Long worldcupId = worldcupService.createWorldcup(title, thumbnailFile, candidatesDataJson, candidateFiles);

		return new WorldcupCreateResponse(worldcupId, "월드컵이 성공적으로 생성되었습니다.");
	}

	/**
	 * GET /api/admin/rank/{worldcupId} : 특정 월드컵의 랭킹 통계를 조회합니다. (Admin 전용)
	 * @param worldcupId 조회할 월드컵 ID (프론트에서 'all'을 보내면 전체 통합 랭킹으로 처리 가능)
	 */
	@GetMapping("/admin/rank/{worldcupId}")
	public List<CandidateRankResponse> getRank(@PathVariable String worldcupId) {
		return worldcupService.calculateAndGetRank(worldcupId);
	}

	//얘도 수정해야됨.
	/**
	 * GET /api/admin/worldcup/{worldcupId} : 특정 월드컵의 상세 정보를 조회합니다. (수정 폼 데이터 로딩용)
	 */
	@GetMapping("/admin/worldcup/{worldcupId}")
	public WorldcupDetailResponse getWorldcupForEdit(@PathVariable Long worldcupId) {
		return worldcupService.findWorldcupDetailForEdit(worldcupId);
	}

	/**
	 * PUT /api/admin/worldcup/{worldcupId} : 특정 월드컵 정보 및 후보를 수정합니다.
	 */
	@PutMapping("/admin/worldcup/{worldcupId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateWorldcup(
		@PathVariable Long worldcupId,
		@RequestPart(value = "title") String title,
		@RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,
		@RequestPart(value = "candidatesDataJson") String candidatesDataJson,
		@RequestPart(value = "candidateFiles", required = false) List<MultipartFile> candidateFiles
	) {
		// null 체크를 위해 빈 리스트 할당
		if (candidateFiles == null) {
			candidateFiles = List.of();
		}

		// 서비스 호출 시 파라미터를 풀어헤쳐서 전달
		worldcupService.updateWorldcup(worldcupId, title, thumbnailFile, candidatesDataJson, candidateFiles);
	}

	/**
	 * DELETE /api/admin/worldcup/{worldcupId} : 특정 월드컵을 삭제합니다. (Admin 전용)
	 */
	@DeleteMapping("/admin/worldcup/{worldcupId}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // 💡 204 No Content
	public void deleteWorldcup(@PathVariable Long worldcupId) {
		worldcupService.deleteWorldcup(worldcupId);
	}
}