package io.github.zeromok.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter @Setter
@ToString
public class User {
	private Long id;
	private String username;
	private String email;
	private String password;
}
