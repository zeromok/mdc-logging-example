package io.github.zeromok.aspect;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class MdcLoggingAspectTest {
	@Test
	void testMdcClearProblem() {
		// Given: Controller AOP에서 traceId 설정
		MDC.put("traceId", "test-123");
		assertThat(MDC.get("traceId")).isEqualTo("test-123");

		// When: Service AOP가 중첩 실행되고 finally에서 MDC.clear() 호출
		simulateServiceAop();

		// Then: Controller로 돌아왔을 때 traceId가 사라짐
		assertThat(MDC.get("traceId")).isNull(); // 💥 문제 발생!
	}

	private void simulateServiceAop() {
		try {
			// Service 로직 실행
			System.out.println("Service logic executing...");
		} finally {
			// Service AOP의 finally 블록
			MDC.clear(); // 💥 여기서 Controller의 MDC도 함께 지워짐!
		}
	}

	@Test
	void testThreadLocalSharing() {
		// Given: 같은 스레드에서 실행되는 중첩 메서드들
		MDC.put("traceId", "shared-123");

		// When: 중첩된 메서드에서 MDC 조회
		String traceIdInNestedMethod = getTraceIdInNestedMethod();

		// Then: 같은 스레드이므로 같은 값을 공유
		assertThat(traceIdInNestedMethod).isEqualTo("shared-123");
		assertThat(MDC.get("traceId")).isEqualTo("shared-123");

		// Cleanup
		MDC.clear();
	}

	private String getTraceIdInNestedMethod() {
		// 중첩된 메서드에서도 같은 ThreadLocal을 공유
		return MDC.get("traceId");
	}
}