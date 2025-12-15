package io.github.zeromok.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Setter @Getter
@ToString
public class LoginRequest {
	private String username;
	private String password;
}
