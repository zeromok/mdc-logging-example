package io.github.zeromok.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thread-name")
class ThreadNameLoggingAspectTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("문제: 순차 요청에서 같은 스레드 재사용")
	void testSequentialRequestsUseSameThread() throws Exception {
		// G: 요청 바디 생성
		String requestBody = """
			{
				"username": "alice",
				"password": "password123"
			}
			""";

		// W: 로그인 요청, 유저 정보 요청을 순차적으로 요청
		mockMvc.perform(post("/mdc/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk());

		Thread.sleep(100);

		mockMvc.perform(get("/mdc/api/users/2"))
			.andExpect(status().isOk());

		// T: 같은 스레드 이름으로 찍힌다. (예상했던 결과 아님)
	}

	@Test
	@DisplayName("문제: 동시 요청에서 스레드 재사용")
	void testConcurrentRequestsShareThreads() throws Exception {
		int numberOfRequests = 20;
		CountDownLatch latch = new CountDownLatch(numberOfRequests);
		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (int i = 1; i <= numberOfRequests; i++) {
			final int userId = i;
			executor.submit(() -> {
				try {
					mockMvc.perform(get("/mdc/api/users/" + (userId % 3 + 1)))
						.andExpect(status().isOk());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();
	}
}