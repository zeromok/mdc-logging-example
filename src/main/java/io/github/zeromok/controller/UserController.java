package io.github.zeromok.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.zeromok.domain.LoginRequest;
import io.github.zeromok.domain.LoginResponse;
import io.github.zeromok.domain.User;
import io.github.zeromok.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/mdc/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		log.info("로그인 요청. for user: {}", request.getUsername());

		try {
			LoginResponse response = userService.authenticate(request);
			log.info("로그인 성공. for user: {}", request.getUsername());
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("로그인 실패. for user: {}", request.getUsername(), e);
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@PathVariable Long id) {
		log.info("유저 정보 요청. for id: {}", id);

		try {
			User user = userService.getUserById(id);
			log.info("유저 정보 검색 완료. 이름: {}", user.getUsername());
			return ResponseEntity.ok(user);

		} catch (Exception e) {
			log.error("유저를 찾을 수 없습니다. with id: {}", id, e);
			return ResponseEntity.notFound().build();
		}
	}
}
