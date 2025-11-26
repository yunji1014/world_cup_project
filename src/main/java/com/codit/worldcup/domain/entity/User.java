package com.codit.worldcup.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // userId 대신 id 사용

	@Column(name = "nickname",
		nullable = false,
		unique = true,
		length = 50,
		columnDefinition = "VARCHAR(50) COLLATE utf8mb4_bin") // 💡 핵심: utf8mb4_bin 사용
	private String nickname;

	@Column(name = "password", nullable = true) // 초기 사용자는 null일 수도 있으므로 nullable을 true로 설정
	private String password;

	// Admin 구분을 위한 역할 필드
	@Column(name = "role", nullable = false, length = 10)
	private String role; // "USER" 또는 "ADMIN"

	@Builder
	public User(String nickname, String role, String password) {
		this.nickname = nickname;
		this.role = role;
		this.password = password;
	}
}