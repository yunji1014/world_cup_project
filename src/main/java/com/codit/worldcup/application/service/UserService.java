package com.codit.worldcup.application.service;

import com.codit.worldcup.domain.entity.User;
import com.codit.worldcup.domain.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) { // 💡 생성자 수정
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User login(String nickname, String rawPassword) {
		Optional<User> existingUserOpt = userRepository.findByNickname(nickname);

		if (existingUserOpt.isPresent()) {
			// 1. 닉네임이 존재하는 경우 (로그인)
			User user = existingUserOpt.get();

			// 💡 비밀번호 일치 여부 확인
			if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
				throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
			}
			return user;

		} else {
			// 2. 닉네임이 존재하지 않는 경우 (가입)

			// 💡 비밀번호 해시
			String encodedPassword = passwordEncoder.encode(rawPassword);

			String role = "user";
			if (nickname.equals("admin")) {
				role = "admin";
			}

			User newUser = User.builder()
				.nickname(nickname)
				.role(role)
				.password(encodedPassword) // 💡 해시된 비밀번호 저장
				.build();
			return userRepository.save(newUser); // DB에 저장 후 반환
		}
	}
}