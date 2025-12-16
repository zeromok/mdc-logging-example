package io.github.zeromok.filter;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mdc-filter")
class MdcLoggingFilterTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testMdcIsApplied() throws Exception {
		// G: 로그인 요청
		String requestBody = """
			{
				"username": "alice",
				"password": "password123"
			}
			""";

		// W: API 호출
		MvcResult result = mockMvc.perform(post("/mdc/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)
			)
			.andExpect(status().isOk())
			.andReturn();

		// T: 응답 확인
		assertThat(result.getResponse().getContentAsString()).isNotEmpty();

		// 테스트 후 MDC가 정리 되었는지 확인
		assertThat(MDC.get("traceId")).isNull();
	}

	@Test
	void testMdcWithExistingTraceId() throws Exception {
		// G: 기존 traceId를 헤더로 전달
		String existingTraceId = "external-trace-123";

		// W: API 호출
		mockMvc.perform(get("/api/users/1")
				.header("X-Trace-Id", existingTraceId))
			.andExpect(status().isOk());

		// T: 기존 traceId가 재사용되었을 것으로 예상

		// 실제 로그를 확인하면 [external-trace-123]로 출력됨
	}

	@Test
	void testConcurrentRequests() throws Exception {
		// G: 동시 요청 시나리오
		int numberOfRequests = 5;

		// W: 여러 요청을 동시에 실행
		for (int i = 1; i <= numberOfRequests; i++) {
			final int userId = i;
			new Thread(() -> {
				try {
					mockMvc.perform(get("/api/users/" + userId))
						.andExpect(status().isOk());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}

		// T: 각 요청이 독립적인 traceId를 가져야 함

		// 로그를 확인하면 5개의 다른 traceId가 출력됨
		Thread.sleep(1000); // 모든 스레드가 완료될 때까지 대기
	}
}