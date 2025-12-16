package io.github.zeromok.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import io.github.zeromok.domain.User;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class UserRepository {
	// 실제 DB 대신 메모리 저장소 사용
	private final Map<Long, User> users = new HashMap<>();

	public UserRepository() {
		// 테스트용 더미 데이터
		users.put(1L, new User(1L, "alice", "alice@example.com", "password123"));
		users.put(2L, new User(2L, "bob", "bob@example.com", "password456"));
		users.put(3L, new User(3L, "charlie", "charlie@example.com", "password789"));
	}

	public Optional<User> findById(Long id) {
		log.debug("ID로 사용자 찾기: {}", id);

		// DB 조회 시뮬레이션 (약간의 지연)
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		User user = users.get(id);
		if (user != null) {
			log.debug("사용자 찾음: {}", user.getUsername());
		} else {
			log.debug("ID로 사용자 찾지 못함: {}", id);
		}

		return Optional.ofNullable(user);
	}

	public Optional<User> findByUsername(String username) {
		log.debug("이름으로 사용자 찾기: {}", username);

		// DB 조회 시뮬레이션
		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		User user = users.values().stream()
			.filter(u -> u.getUsername().equals(username))
			.findFirst()
			.orElse(null);

		if (user != null) {
			log.debug("사용자 찾음: {}", user.getId());
		} else {
			log.debug("이름으로 사용자 찾지 못함: {}", username);
		}

		return Optional.ofNullable(user);
	}
}
