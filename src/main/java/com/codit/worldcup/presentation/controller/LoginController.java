package com.codit.worldcup.presentation.controller;

import com.codit.worldcup.application.service.UserService;
import com.codit.worldcup.domain.entity.User;
import com.codit.worldcup.presentation.dto.UserLoginRequest;
import com.codit.worldcup.presentation.dto.UserLoginResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {

	private final UserService userService;

	public LoginController(UserService userService) {
		this.userService = userService;
	}

	// DTO를 사용해 요청과 응답의 형태를 명확히 합니다.
	@PostMapping("/login")
	public UserLoginResponse login(@RequestBody UserLoginRequest request) {
		// 💡 닉네임과 비밀번호를 모두 서비스로 전달
		User user = userService.login(request.getNickname(), request.getPassword());
		return new UserLoginResponse(user.getId(), user.getNickname());
	}
}