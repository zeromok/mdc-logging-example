package io.github.zeromok.aspect;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/// 실패 케이스 2: AOP에서 MDC 관리 시도
/// 문제점:
/// 1. AOP는 중첩 실행된다 (Controller → Service → Repository)
/// 2. MDC는 ThreadLocal 기반으로 스레드당 하나의 저장소를 공유한다
/// 3. Service의 finally 블록에서 MDC.clear() 호출 시, Controller의 MDC도 함께 지워진다
/// 실행 순서:
/// 1. Controller AOP 진입 → MDC.put("traceId", "abc123")
/// 2. Controller → Service 호출 (proceed())
/// 3. Service AOP 진입 → 로그 정상 출력 (traceId=abc123)
/// 4. Service AOP 종료 (finally) → MDC.clear()
/// 5. Controller AOP로 복귀 → traceId = null!
@Slf4j
@Aspect
@Profile("mdc-aspect")
public class MdcLoggingAspect {

	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	public void controllerPointcut() {}

	@Pointcut("within(@org.springframework.stereotype.Service *)")
	public void servicePointcut() {}

	@Around("controllerPointcut()")
	public Object controllerLog(ProceedingJoinPoint joinPoint) throws Throwable {
		// traceId 생성 및 MDC에 저장
		String traceId = UUID.randomUUID().toString().substring(0, 8);
		MDC.put("traceId", traceId);

		String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		try {
			log.info("[{}] Controller.{} - BEGIN", className, methodName);

			// 여기까지는 traceId가 정상적으로 출력됨
			Object result = joinPoint.proceed();

			// Service의 finally 블록에서 MDC.clear()가 호출되어 traceId가 사라짐!
			log.info("[{}] Controller.{} - END", className, methodName);
			return result;

		} finally {
			// Controller 종료 시 MDC 정리
			MDC.clear();
		}
	}

	@Around("servicePointcut()")
	public Object serviceLog(ProceedingJoinPoint joinPoint) throws Throwable {
		String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		try {
			log.info("[{}] Service.{} - BEGIN", className, methodName);
			Object result = joinPoint.proceed();
			log.info("[{}] Service.{} - END", className, methodName);
			return result;

		} finally {
			// 여기서 MDC.clear() 호출 시 Controller의 MDC도 함께 지워짐!
			// 같은 스레드의 ThreadLocal을 공유하기 때문
			MDC.clear();
		}
	}
}
