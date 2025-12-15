package io.github.zeromok.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter @Setter
@ToString
public class LoginResponse {
	private Long userId;
	private String token;
	private String message;

	public LoginResponse(Long userId, String token) {
		this.userId = userId;
		this.token = token;
		this.message = "로그인 성공";
	}
}
