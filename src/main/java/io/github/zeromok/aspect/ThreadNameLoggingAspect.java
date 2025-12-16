package io.github.zeromok.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/// 실패 케이스 1: 스레드 이름으로 요청 추적 시도
/// 문제점:
/// 1. 톰캣은 스레드 풀을 사용하여 스레드를 재사용한다
/// 2. 다른 요청이 같은 스레드를 사용할 수 있다
/// 3. 같은 스레드 이름 != 같은 요청
/// 증상:
/// - 완전히 다른 사용자의 로그가 같은 스레드 이름으로 출력됨
/// - 요청 추적 불가능
@Slf4j
@Aspect
@Profile("thread-name")
public class ThreadNameLoggingAspect {

	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	public void controllerPointcut() {}

	@Pointcut("within(@org.springframework.stereotype.Service *)")
	public void servicePointcut() {}

	@Around("controllerPointcut()")
	public Object controllerLog(ProceedingJoinPoint joinPoint) throws Throwable {
		String threadName = Thread.currentThread().getName();
		String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		// 스레드 이름만으로 추적 - 재사용되는 스레드 풀에서는 무의미
		log.info("[{}] Controller.{}.{} - BEGIN",
			threadName, className, methodName);

		Object result = joinPoint.proceed();

		log.info("[{}] Controller.{}.{} - END",
			threadName, className, methodName);

		return result;
	}

	@Around("servicePointcut()")
	public Object serviceLog(ProceedingJoinPoint joinPoint) throws Throwable {
		String threadName = Thread.currentThread().getName();
		String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		log.info("[{}] Service.{}.{} - BEGIN",
			threadName, className, methodName);

		Object result = joinPoint.proceed();

		log.info("[{}] Service.{}.{} - END",
			threadName, className, methodName);

		return result;
	}
}