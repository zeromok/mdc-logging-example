package io.github.zeromok.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("mdc-filter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

	private static final String TRACE_ID = "traceId";
	private static final String REQUEST_METHOD = "method";
	private static final String REQUEST_URI = "uri";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		// 1. traceId 생성 (기존 헤더가 있으면 재사용)
		String traceId = request.getHeader("X-Trace-Id");
		if (traceId == null || traceId.isEmpty()) {
			traceId = UUID.randomUUID().toString().substring(0, 8);
		}

		// 2. MDC에 컨텍스트 정보 저장
		MDC.put(TRACE_ID, traceId);
		MDC.put(REQUEST_METHOD, request.getMethod());
		MDC.put(REQUEST_URI, request.getRequestURI());

		// 3. 요청 시작 로그
		log.info("Request started - {} {}", request.getMethod(), request.getRequestURI());

		long startTime = System.currentTimeMillis();

		try {
			// 4. 다음 필터 체인 실행
			// 이후 실행되는 모든 코드(Controller, Service, Repository)는
			// 같은 스레드에서 실행되므로 동일한 MDC를 공유한다
			filterChain.doFilter(request, response);

		} finally {
			// 5. 요청 종료 로그
			long duration = System.currentTimeMillis() - startTime;
			log.info("Request completed - status: {}, duration: {}ms",
				response.getStatus(), duration);

			// 6. MDC 정리 (요청당 딱 한 번)
			// 스레드 풀에서 재사용되는 스레드이므로 반드시 정리해야 함
			MDC.clear();
		}
	}
}
