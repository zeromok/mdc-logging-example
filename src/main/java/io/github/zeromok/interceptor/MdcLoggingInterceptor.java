package io.github.zeromok.interceptor;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MdcLoggingInterceptor implements HandlerInterceptor {

	private static final String TRACE_ID = "traceId";
	private static final String REQUEST_METHOD = "method";
	private static final String REQUEST_URI = "uri";
	private static final String START_TIME = "startTime";

	// Controller 실행 전
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		Exception {

		// 1. traceId 생성
		String traceId = request.getHeader("X-Trace-Id");
		if (traceId == null || traceId.isEmpty()) {
			traceId = UUID.randomUUID().toString().substring(0, 8);
		}

		// 2. MDC 설정
		MDC.put(TRACE_ID, traceId);
		MDC.put(REQUEST_METHOD, request.getMethod());
		MDC.put(REQUEST_URI, request.getRequestURI());

		// 3. 요청 시작 시간 저장
		request.setAttribute(START_TIME, System.currentTimeMillis());

		log.info("Request started - {} {}", request.getMethod(), request.getRequestURI());

		return true;
	}

	// Controller 실행 후, 예외가 발생하면 호출되지 않음
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
		ModelAndView modelAndView) throws Exception {

		log.info("Request processing completed");
	}

	// View 렌더링 후 호출, 예외 발생 시에도 호출됨
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
		Exception ex) throws Exception {
		try {
			// 요청 처리 시간 계산
			Long startTime = (Long)request.getAttribute(START_TIME);

			if (startTime != null) {
				long duration = System.currentTimeMillis() - startTime;
				log.info("Request completed  - status: {}, duration: {}ms", response.getStatus(), duration);
			}

			// 예외 발생 시 로깅
			if (ex != null) {
				log.error("Request failed exception: ", ex);
			}
		} finally {
			// MDC 정리
			MDC.clear();
		}
	}
}
