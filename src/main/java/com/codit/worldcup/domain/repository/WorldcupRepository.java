package com.codit.worldcup.domain.repository;

import com.codit.worldcup.domain.entity.Worldcup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldcupRepository extends JpaRepository<Worldcup, Long> {
	// 추가적인 쿼리 없이 기본 CRUD만 사용합니다.
}