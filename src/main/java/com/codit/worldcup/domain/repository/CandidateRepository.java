package com.codit.worldcup.domain.repository;

import java.util.List;

import com.codit.worldcup.domain.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
	// 월드컵 ID로 모든 후보를 찾는 메서드 (월드컵 시작 시 사용)
	List<Candidate> findAllByWorldcupId(Long worldcupId);
	void deleteAllByWorldcupId(Long worldcupId);
}