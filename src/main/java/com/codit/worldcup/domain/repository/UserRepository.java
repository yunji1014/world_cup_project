package com.codit.worldcup.domain.repository;

import com.codit.worldcup.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	// 닉네임으로 사용자를 찾는 쿼리 메서드 정의 (로그인 시 사용)
	Optional<User> findByNickname(String nickname);
}