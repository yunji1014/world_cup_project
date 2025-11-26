package com.codit.worldcup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		// 💡 BCrypt는 강력한 해시 알고리즘을 사용하여 비밀번호를 저장합니다.
		return new BCryptPasswordEncoder();
	}
}
