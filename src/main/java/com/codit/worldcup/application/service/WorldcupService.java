package com.codit.worldcup.application.service;

import com.codit.worldcup.domain.entity.Candidate;
import com.codit.worldcup.domain.entity.Comment;
import com.codit.worldcup.domain.entity.User;
import com.codit.worldcup.domain.entity.UserResult;
import com.codit.worldcup.domain.entity.Worldcup;
import com.codit.worldcup.domain.repository.CandidateRepository;
import com.codit.worldcup.domain.repository.CommentRepository;
import com.codit.worldcup.domain.repository.UserRepository;
import com.codit.worldcup.domain.repository.UserResultRepository;
import com.codit.worldcup.domain.repository.WorldcupRepository;
import com.codit.worldcup.presentation.dto.CandidateRankResponse;
import com.codit.worldcup.presentation.dto.CommentResponse;
import com.codit.worldcup.presentation.dto.WorldcupCreateRequest;
import com.codit.worldcup.presentation.dto.WorldcupDetailResponse;
import com.codit.worldcup.presentation.dto.WorldcupListResponse;
import com.codit.worldcup.presentation.dto.WorldcupResultResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class WorldcupService {

	private final UserResultRepository userResultRepository;
	private final WorldcupRepository worldcupRepository;
	private final CandidateRepository candidateRepository; // 후보 저장을 위해 추가
	private final UserRepository userRepository;
	private final CommentRepository commentRepository;
	private final S3Service s3Service;
	private final ObjectMapper objectMapper;

	public WorldcupService(UserResultRepository userResultRepository, WorldcupRepository worldcupRepository, CandidateRepository candidateRepository, UserRepository userRepository, CommentRepository commentRepository, S3Service s3Service) {
		this.userResultRepository = userResultRepository;
		this.worldcupRepository = worldcupRepository;
		this.candidateRepository = candidateRepository;
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
		this.s3Service = s3Service;
		this.objectMapper = new ObjectMapper();
	}

	/**
	 * 월드컵 목록을 조회하여 DTO 형태로 반환합니다.
	 */
	public List<WorldcupListResponse> findAllWorldcups() {
		List<Worldcup> worldcups = worldcupRepository.findAll(); // 모든 월드컵 조회

		// Entity를 프론트엔드 응답용 DTO로 변환
		return worldcups.stream()
			.map(wc -> new WorldcupListResponse(
				wc.getId(),
				wc.getTitle(),
				wc.getThumbnailUrl() // 썸네일 URL 반환
			))
			.collect(Collectors.toList());
	}

	/**
	 * 월드컵 정보를 저장하고 후보 목록을 함께 저장합니다.
	 * 💡 [새로운 메서드] MultipartFile 파일 업로드 로직이 통합되었습니다.
	 * @param title 월드컵 제목
	 * @param thumbnailFile 썸네일 파일
	 * @param candidatesDataJson 후보 데이터 (JSON 문자열)
	 * @param candidateFiles 후보 이미지 파일 리스트
	 * @return 생성된 월드컵 ID
	 */
	@Transactional
	public Long createWorldcup(String title, MultipartFile thumbnailFile, String candidatesDataJson, List<MultipartFile> candidateFiles) {

		// 1. JSON 파싱
		WorldcupCreateRequest request = objectMapper.readValue(candidatesDataJson, WorldcupCreateRequest.class);

		// 💡 파일 리스트가 null일 경우 빈 리스트로 방어
		if (candidateFiles == null) candidateFiles = List.of();

		// 2. 썸네일 업로드
		String thumbnailUrl = null;
		if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
			thumbnailUrl = s3Service.uploadFile(thumbnailFile, "worldcup/thumbnails/");
		}

		// 3. 월드컵 저장
		Worldcup worldcup = Worldcup.builder()
			.title(title)
			.thumbnailUrl(thumbnailUrl)
			.build();
		worldcup = worldcupRepository.save(worldcup);
		Long worldcupId = worldcup.getId();

		// 4. 후보 저장 (💡 핵심 수정: 파일 인덱스를 별도로 관리)
		int fileIndex = 0; // 파일 리스트를 가리키는 커서

		for (WorldcupCreateRequest.CandidateDto dto : request.getCandidates()) {
			String imageUrl = "";

			// 파일이 남아있다면 하나 꺼내서 업로드
			if (fileIndex < candidateFiles.size()) {
				MultipartFile file = candidateFiles.get(fileIndex++);
				imageUrl = s3Service.uploadFile(file, "worldcup/candidates/");
			} else {
				// ⚠️ 파일이 부족한 경우 처리 (기본 이미지 혹은 예외)
				// 여기서는 로그를 남기거나, 빈 문자열로 진행합니다.
				System.out.println("경고: 후보 데이터보다 파일이 부족합니다. 이름: " + dto.getName());
			}

			Candidate candidate = Candidate.builder()
				.worldcupId(worldcupId)
				.name(dto.getName())
				.imageUrl(imageUrl)
				.build();
			candidateRepository.save(candidate);
		}

		return worldcupId;

	}

	@Transactional(readOnly = true)
	public WorldcupDetailResponse findWorldcupDetail(Long worldcupId) {

		// 1. 월드컵 기본 정보 조회
		Worldcup worldcup = worldcupRepository.findById(worldcupId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 월드컵 ID입니다: " + worldcupId));

		// 2. 해당 월드컵의 모든 후보 목록 조회
		List<Candidate> candidates = candidateRepository.findAllByWorldcupId(worldcupId);

		if (candidates.size() < 2) {
			// 프론트엔드가 요구하는 후보 부족 조건 처리
			throw new IllegalArgumentException("후보가 부족한 월드컵입니다.");
		}

		// 3. DTO로 변환
		List<WorldcupDetailResponse.CandidateDto> candidateDtos = candidates.stream()
			.map(c -> new WorldcupDetailResponse.CandidateDto(
				c.getId(),
				c.getName(),
				c.getImageUrl()))
			.collect(Collectors.toList());

		return new WorldcupDetailResponse(worldcup.getId(), worldcup.getTitle(), worldcup.getThumbnailUrl(), candidateDtos);
	}

	/**
	 * 사용자의 선택을 기록하고 통계를 업데이트합니다.
	 * @param worldcupId 현재 월드컵 ID
	 * @param winnerId 선택된 후보 ID
	 * @param loserId 탈락된 후보 ID
	 * @param round 현재 진행 라운드 수
	 */
	@Transactional
	public void recordSelection(Long worldcupId, Long winnerId, Long loserId, int round, Long userId) {

		// 1. 선택된 후보의 전체 클릭 수 증가 (랭킹 조건 2)
		Candidate winner = candidateRepository.findById(winnerId)
			.orElseThrow(() -> new IllegalArgumentException("Winner ID를 찾을 수 없습니다."));
		winner.incrementTotalSelectionCount(); // Candidate.java에 구현된 메서드 사용
		candidateRepository.save(winner);

		// 2. 최종 라운드(결승)인 경우 처리 (프론트에서 round=2 로 넘어왔다고 가정)
		if (round == 2) {
			// 2-1. 1등으로 뽑힌 횟수 증가 (랭킹 조건 1)
			winner.incrementWinCount();
			candidateRepository.save(winner);

			// 2-2. 사용자 최종 결과 기록 (랭킹 조건 4의 닉네임 조회를 위해 사용)
			// ⚠️ 주의: 현재 로직에는 userId가 없으므로, 프론트에서 보내도록 수정하거나,
			//         여기서는 임시로 userId를 1로 가정합니다. (실제 구현 시 로그인 정보를 사용해야 함)
			//Long dummyUserId = 1L; // 💡 실제 구현 시 로그인 세션/토큰에서 가져와야 함.

			UserResult userResult = UserResult.builder()
				.userId(userId)
				.worldcupId(worldcupId)
				.winnerId(winnerId)
				.build();
			userResultRepository.save(userResult);
		}
	}

	/**
	 * 특정 월드컵의 최종 결과 정보 (가장 많이 1등한 후보와 그 사용자의 닉네임)를 조회합니다.
	 */
	public WorldcupResultResponse findFinalResult(Long worldcupId, Long winnerId) {

		// 1. 우승 후보 정보 조회 (URL에서 받은 ID 사용)
		Candidate finalWinner = candidateRepository.findById(winnerId)
			.orElseThrow(() -> new IllegalArgumentException("최종 우승자 ID를 찾을 수 없습니다."));
		// 2. 해당 우승 후보를 선택한 모든 사용자의 최종 결과 기록 조회 (기존 로직 유지)
		List<UserResult> winnerResults = userResultRepository.findAllByWorldcupId(worldcupId).stream()
			.filter(r -> r.getWinnerId().equals(finalWinner.getId()))
			.collect(Collectors.toList());

		// 3. 사용자 ID 목록을 추출하여 닉네임 조회 (랭킹 조건 4)
		List<Long> winnerUserIds = winnerResults.stream()
			.map(UserResult::getUserId)
			.distinct() // 중복 사용자 제거
			.collect(Collectors.toList());

		List<String> topWinnerNicknames = userRepository.findAllById(winnerUserIds).stream()
			.map(user -> {
				// 관리자 닉네임은 특수하게 표시하도록 프론트엔드와 약속할 수 있습니다.
				return user.getNickname().equals("admin") ? "관리자" : user.getNickname();
			})
			.collect(Collectors.toList());


		// 4. 응답 DTO 생성
		WorldcupResultResponse.WinnerCandidateDto winnerDto = new WorldcupResultResponse.WinnerCandidateDto(
			finalWinner.getId(),
			finalWinner.getName(),
			finalWinner.getImageUrl()
		);

		return new WorldcupResultResponse(winnerDto, topWinnerNicknames);
	}

	/**
	 * 특정 월드컵의 댓글 목록을 조회하여 DTO로 변환합니다.
	 */
	@Transactional(readOnly = true)
	public List<CommentResponse> findCommentsByWorldcup(Long worldcupId) {
		List<Comment> comments = commentRepository.findAllByWorldcupIdOrderByCreatedAtDesc(worldcupId);

		return comments.stream().map(comment -> {
			// 💡 닉네임 조회가 필요합니다.
			String nickname = userRepository.findById(comment.getUserId())
				.map(User::getNickname)
				.orElse("탈퇴한 사용자");

			return new CommentResponse(
				comment.getId(),
				nickname,
				comment.getContent(),
				comment.getCreatedAt()
			);
		}).collect(Collectors.toList());
	}

	/**
	 * 댓글을 DB에 저장합니다.
	 */
	@Transactional
	public void addComment(Long worldcupId, Long userId, String content) {
		Comment comment = Comment.builder()
			.worldcupId(worldcupId)
			.userId(userId)
			.content(content)
			.build();
		commentRepository.save(comment);
	}

	/**
	 * 특정 월드컵의 랭킹을 계산하고 정렬하여 반환합니다.
	 */
	@Transactional(readOnly = true)
	public List<CandidateRankResponse> calculateAndGetRank(String worldcupId) {

		// 1. 후보 조회 로직 (기존과 동일)
		List<Candidate> candidates;
		if (worldcupId.equalsIgnoreCase("all")) {
			candidates = candidateRepository.findAll();
		} else {
			try {
				Long id = Long.parseLong(worldcupId);
				candidates = candidateRepository.findAllByWorldcupId(id);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("유효하지 않은 월드컵 ID 형식입니다: " + worldcupId);
			}
		}

		// 💡 [수정됨] 정렬 로직
		// Comparator.reverseOrder()를 사용하여 해당 필드만 명확하게 내림차순으로 지정합니다.
		Comparator<Candidate> rankComparator = Comparator
			// 1. 1등 횟수 (WinCount): 내림차순 (큰 수가 위로)
			.comparing(Candidate::getWinCount, Comparator.reverseOrder())
			// 2. 총 클릭 수 (TotalSelectionCount): 내림차순 (큰 수가 위로)
			.thenComparing(Candidate::getTotalSelectionCount, Comparator.reverseOrder())
			// 3. 후보 이름 (Name): 오름차순 (가나다/abc 순)
			.thenComparing(Candidate::getName);

		// 정렬 수행
		candidates.sort(rankComparator);

		// 3. 응답 DTO 변환 로직 (기존과 동일)
		return candidates.stream().map(candidate -> {
			List<String> topWinnerNicknames = userResultRepository.findAllByWorldcupId(candidate.getWorldcupId()).stream()
				.filter(r -> r.getWinnerId().equals(candidate.getId()))
				.map(UserResult::getUserId)
				.distinct()
				.flatMap(userId -> userRepository.findById(userId).stream())
				.map(user -> user.getNickname().equals("admin") ? "관리자" : user.getNickname())
				.collect(Collectors.toList());

			return new CandidateRankResponse(
				candidate.getId(),
				candidate.getName(),
				candidate.getWinCount(),
				candidate.getTotalSelectionCount(),
				topWinnerNicknames
			);
		}).collect(Collectors.toList());
	}

	/**
	 * 특정 월드컵과 관련된 모든 데이터를 삭제합니다. (Admin 전용)
	 */
	@Transactional // DB 변경이 일어나므로 @Transactional 명시
	public void deleteWorldcup(Long worldcupId) {
		// 1. 월드컵 존재 확인 (필요 시)
		if (!worldcupRepository.existsById(worldcupId)) {
			throw new IllegalArgumentException("존재하지 않는 월드컵 ID입니다: " + worldcupId);
		}

		// 2. 관련 데이터 삭제
		// 💡 deleteAllBy... 메서드는 Repository 인터페이스에 정의되어 있어야 합니다.
		candidateRepository.deleteAllByWorldcupId(worldcupId);
		userResultRepository.deleteAllByWorldcupId(worldcupId);
		commentRepository.deleteAllByWorldcupId(worldcupId);

		// 3. 월드컵 자체 삭제
		worldcupRepository.deleteById(worldcupId);
	}

	// 💡 1. 수정 폼에 데이터 로딩을 위한 서비스 메서드 (기존 findWorldcupDetail 재활용 가능)
	@Transactional(readOnly = true)
	public WorldcupDetailResponse findWorldcupDetailForEdit(Long worldcupId) {
		// 기존의 findWorldcupDetail(Long worldcupId) 로직을 재활용합니다.
		// WorldcupDetailResponse DTO가 후보 ID, 이름, URL을 모두 포함하도록 이미 설계되어 있습니다.
		return findWorldcupDetail(worldcupId);
	}

	// 💡 2. 수정 데이터 반영 서비스 메서드
	@Transactional
	public void updateWorldcup(Long worldcupId, String title, MultipartFile thumbnailFile, String candidatesDataJson, List<MultipartFile> candidateFiles) {

		// 1. Worldcup 기본 정보 수정
		Worldcup worldcup = worldcupRepository.findById(worldcupId)
			.orElseThrow(() -> new IllegalArgumentException("수정할 월드컵을 찾을 수 없습니다."));

		// 썸네일 파일이 새로 들어왔으면 업로드 후 URL 갱신
		String thumbnailUrl = worldcup.getThumbnailUrl();
		if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
			thumbnailUrl = s3Service.uploadFile(thumbnailFile, "worldcup/thumbnails/");
		}

		worldcup.update(title, thumbnailUrl);
		worldcupRepository.save(worldcup);

		// 2. 후보 데이터 JSON 파싱
		WorldcupCreateRequest request = objectMapper.readValue(candidatesDataJson, WorldcupCreateRequest.class);

		// 💡 파일 처리를 위한 인덱스 (파일 리스트에서 하나씩 꺼내 쓰기 위함)
		int fileListIndex = 0;

		// 3. 기존 후보 목록 조회 (삭제 처리용)
		List<Candidate> existingCandidates = candidateRepository.findAllByWorldcupId(worldcupId);
		List<Long> requestedIds = request.getCandidates().stream()
			.map(c -> {
				try { return Long.parseLong(c.getId()); } catch (Exception e) { return null; }
			})
			.filter(id -> id != null)
			.collect(Collectors.toList());

		// 3-1. 삭제할 후보 처리
		existingCandidates.stream()
			.filter(c -> !requestedIds.contains(c.getId()))
			.forEach(candidateRepository::delete);

		// 4. 후보 추가 및 수정 루프
		for (WorldcupCreateRequest.CandidateDto dto : request.getCandidates()) {
			Long candidateId = null;
			try {
				if (dto.getId() != null && !dto.getId().isEmpty() && !dto.getId().equals("undefined")) {
					candidateId = Long.parseLong(dto.getId());
				}
			} catch (NumberFormatException e) { /* 신규 후보 */ }

			// 이미지 URL 결정 로직
			String finalImageUrl = dto.getImagePath(); // 기본적으로 프론트에서 보낸 기존 URL 유지

			// 💡 프론트엔드에서 "이 후보는 이미지가 변경되었습니다"라고 보낸 경우 (약속된 플래그 확인 필요, 여기서는 imagePath가 비었거나 파일을 보냈다고 가정)
			// 하지만 더 확실한 방법은, 프론트에서 파일을 보냈다면 candidateFiles 리스트를 소비하는 것입니다.
			// 여기서는 "새로운 파일이 필요한 경우"를 dto의 상태나 별도 플래그로 판단해야 하지만,
			// 간단하게 구현하기 위해: dto.getImagePath()가 비어있거나 'NEW' 마킹이 있으면 파일 리스트에서 꺼낸다고 가정합니다.

			// 💡 더 강력한 로직: 프론트엔드에서 파일 변경 시 imagePath를 비워서 보낸다고 가정
			boolean isNewFile = (dto.getImagePath() == null || dto.getImagePath().isEmpty());

			if (isNewFile) {
				if (fileListIndex < candidateFiles.size()) {
					MultipartFile file = candidateFiles.get(fileListIndex++);
					finalImageUrl = s3Service.uploadFile(file, "worldcup/candidates/");
				} else {
					// 파일이 부족한 경우 (예외처리 혹은 무시)
				}
			}

			if (candidateId == null) {
				// 신규 추가
				Candidate newCandidate = Candidate.builder()
					.worldcupId(worldcupId)
					.name(dto.getName())
					.imageUrl(finalImageUrl)
					.build();
				candidateRepository.save(newCandidate);
			} else {
				// 기존 수정
				Candidate existingCandidate = candidateRepository.findById(candidateId)
					.orElseThrow(() -> new IllegalArgumentException("후보 없음"));

				existingCandidate.update(dto.getName(), finalImageUrl);
				candidateRepository.save(existingCandidate);
			}
		}

	}
}