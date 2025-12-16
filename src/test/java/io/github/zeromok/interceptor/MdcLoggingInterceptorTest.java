package io.github.zeromok.interceptor;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mdc-interceptor")
class MdcLoggingInterceptorTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("정상 케이스: Interceptor에서 MDC가 적용됨")
	void testInterceptorMdcWorks() throws Exception {
		// G
		String requestBody = """
			{
				"username": "alice",
				"password": "password123"
			}
			""";

		// W: 정상적인 API 호출
		mockMvc.perform(post("/mdc/api/users/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody)
		).andExpect(status().isOk());

		// T: 테스트 후 MDC가 정리되었는지 확인
		assertThat(MDC.get("traceId")).isNull();
	}

	@Test
	@DisplayName("기존 traceId 헤더가 있으면 재사용함")
	void testInterceptorReusesExistingTraceId() throws Exception {
		// G: 외부에서 전달된 traceId
		String externalTraceId = "external-abc123";

		// W: 헤더에 traceId를 포함하여 요청
		mockMvc.perform(get("/mdc/api/users/1")
			.header("X-Trace-Id", externalTraceId)
		).andExpect(status().isOk());
	}

	@Test
	@DisplayName("한계 1: DispatcherServlet 이전의 Filter는 MDC 없음")
	void testInterceptorMissesFilterChain() throws Exception {

		// W: Filter가 있다면 그 Filter의 로그에는 traceId가 없을 것
		mockMvc.perform(get("/mdc/api/users/1"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("한계 2: 404 등 DispatcherServlet에서 처리되는 경우는 추적 불가")
	void testInterceptorMissesDispatcherServletErrors() throws Exception {

		// W: 존재하지 않는 경로
		// 404 에러 발생 -> preHandle() 호출되지 않음
		mockMvc.perform(get("/no-path"))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("예외 발생 시에도 afterCompletion은 호출됨")
	void testInterceptorHandlesException() throws Exception {
		// G
		String requestBody = """
			{
				"username": "alice",
				"password": "xxxx"
			}
			""";

		// W: 잘못된 비밀번호로 로그인 시도 (예외 발생)
		mockMvc.perform(post("/mdc/api/users/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody)
		).andExpect(status().isBadRequest());

		// T: MDC 정리 확인
		assertThat(MDC.get("traceId")).isNull();
	}

	@Test
	@DisplayName("동시 요청에서도 각자 독립적인 traceId 유지")
	void testConcurrentRequests() throws Exception {

		// 여러 요청을 동시에 실행
		for (int i = 1; i <= 5; i++) {
			final int userId = i;
			new Thread(() -> {
				try {
					mockMvc.perform(get("/mdc/api/users/" + userId))
						.andExpect(status().isOk());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}

		Thread.sleep(1000);
	}
}