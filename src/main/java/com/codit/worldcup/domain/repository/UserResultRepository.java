package com.codit.worldcup.domain.repository;

import java.util.List;

import com.codit.worldcup.domain.entity.UserResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserResultRepository extends JpaRepository<UserResult, Long> {
	// 특정 월드컵의 우승자를 뽑은 모든 기록을 조회 (랭킹 조건 4에 사용)
	List<UserResult> findAllByWorldcupId(Long worldcupId);
	void deleteAllByWorldcupId(Long worldcupId);
}