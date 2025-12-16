package io.github.zeromok.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github.zeromok.domain.LoginRequest;
import io.github.zeromok.domain.LoginResponse;
import io.github.zeromok.domain.User;
import io.github.zeromok.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public LoginResponse authenticate(LoginRequest request) {
		log.info("사용자 인증: {}", request.getUsername());

		// 사용자 조회
		User user = userRepository.findByUsername(request.getUsername())
			.orElseThrow(() -> {
				log.error("사용자 없음: {}", request.getUsername());
				return new RuntimeException("사용자 없음");
			});

		log.debug("비밀번호가 맞지 않습니다.");

		// 비밀번호 검증 (실제로는 암호화된 비밀번호 비교)
		if (!user.getPassword().equals(request.getPassword())) {
			log.error("비밀번호를 확인해주세요. ID: {}", request.getUsername());
			throw new RuntimeException("비밀번호를 확인해주세요.");
		}

		log.debug("비밀번호 일치");

		// 토큰 생성
		String token = generateToken(user);
		log.info("사용자 인증 완료. user: {}", user.getUsername());

		return new LoginResponse(user.getId(), token);
	}

	public User getUserById(Long id) {
		log.info("사용자 찾기. ID: {}", id);

		User user = userRepository.findById(id)
			.orElseThrow(() -> {
				log.error("사용자를 찾을 수 없습니다. ID: {}", id);
				return new RuntimeException("사용자를 찾을 수 없습니다.");
			});

		log.info("사용자 찾음. 이름: {}", user.getUsername());
		return user;
	}

	private String generateToken(User user) {
		log.debug("사용자를 위한 토큰 생성: {}", user.getId());

		// 실제로는 JWT 등을 사용
		String token = "TOKEN-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);

		log.debug("토큰 생성 완료");
		return token;
	}
}
