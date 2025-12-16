# MDC를 통한 요청 추적기
> 블로그 포스트: [MDC를 통한 요청 추적기](https://b-mokk.tistory.com/81)


Spring Boot에서 MDC(Mapped Diagnostic Context)를 활용한 요청 추적 시스템 구축 과정을 다룬 예제 프로젝트입니다.
스레드 이름 기반 추적 → AOP 기반 MDC → Interceptor 기반 MDC → Filter 기반 MDC까지,
실패와 성공의 전 과정을 실행 가능한 코드로 제공합니다.

## 📌 프로젝트 개요

### 핵심 주제
- 문제: 동시 요청 환경에서 로그가 섞여 요청 추적 불가
- 시도 1: 스레드 이름으로 구분 → ❌ 톰캣 스레드 풀 재사용 문제
- 시도 2: AOP에서 MDC 관리 → ❌ AOP 중첩 실행 시 MDC.clear() 충돌 
- 시도 3: Interceptor에서 MDC 관리 → △ 제한적 (404 에러 등 미추적)
- 최종 해결: Filter에서 MDC 관리 → ✅ 요청 전체 생명주기 커버

### 기술 스택
- Spring Boot 3.5.8
- Java 17
- Gradle 8.14.3
- Logback (SLF4J + MDC)
- JMH 1.37 (성능 벤치마크)

---
## 🗂️ 프로젝트 구조
```
mdc-logging-example/
├── src/
│   ├── main/java/io/github/zeromok/
│   │   ├── MdcLoggingExampleApplication.java  # 메인 애플리케이션
│   │   │
│   │   ├── aspect/
│   │   │   ├── ThreadNameLoggingAspect.java   # ❌ 실패 케이스 1: 스레드 이름
│   │   │   └── MdcLoggingAspect.java          # ❌ 실패 케이스 2: AOP MDC
│   │   │
│   │   ├── interceptor/
│   │   │   └── MdcLoggingInterceptor.java     # △ 제한적: Interceptor MDC
│   │   │
│   │   ├── filter/
│   │   │   └── MdcLoggingFilter.java          # ✅ 최종 해결책: Filter MDC
│   │   │
│   │   ├── config/
│   │   │   ├── LoggingConfig.java             # Profile별 Bean 등록
│   │   │   └── WebMvcConfig.java              # Interceptor 설정
│   │   │
│   │   ├── controller/
│   │   │   └── UserController.java            # REST API
│   │   ├── service/
│   │   │   └── UserService.java               # 비즈니스 로직
│   │   ├── repository/
│   │   │   └── UserRepository.java            # 데이터 접근
│   │   └── domain/
│   │       ├── User.java
│   │       ├── LoginRequest.java
│   │       └── LoginResponse.java
│   │
│   ├── test/java/io/github/zeromok/
│   │   ├── aspect/
│   │   │   ├── ThreadNameLoggingAspectTest.java  # 스레드 재사용 문제 재현
│   │   │   └── MdcLoggingAspectTest.java         # AOP 중첩 문제 재현
│   │   ├── filter/
│   │   │   └── MdcLoggingFilterTest.java         # Filter 정상 동작 검증
│   │   └── interceptor/
│   │       └── MdcLoggingInterceptorTest.java    # Interceptor 한계 테스트
│   │
│   ├── jmh/java/io/github/zeromok/benchmark/
│   │   └── MdcPerformanceBenchmark.java          # JMH 성능 벤치마크
│   │
│   └── main/resources/
│       ├── application.yml                       # 로그 레벨 설정
│       └── logback-spring.xml                    # MDC 로깅 패턴
│
├── build.gradle                                   # Gradle 설정
└── README.md
```

---

## 🚀 실행 방법
### 사전 요구사항
- Java 17+
- Gradle 8.x+

### 프로젝트 클론
```bash
git clone https://github.com/zeromok/mdc-logging-example.git
cd mdc-logging-example
```

### 각 방식별 실행
#### 스레드 이름 방식
```bash
./gradlew bootRun --args='--spring.profiles.active=thread-name'
```
문제 확인:
- 서로 다른 요청이 같은 스레드 이름으로 출력됨
- 톰캣 스레드 풀이 스레드를 재사용하기 때문

#### AOP MDC 방식
```bash
./gradlew bootRun --args='--spring.profiles.active=mdc-aspect'
```
문제 확인:
- Controller END 로그에서 traceId가 사라짐
- Service AOP의 `MDC.clear()` 가 Controller에도 영향

#### Interceptor MDC 방식
```bash
./gradlew bootRun --args='--spring.profiles.active=mdc-interceptor'
```
한계:
- 정상 요청은 추적 가능
- 404 에러 등 DispatcherServlet 레벨 문제는 추적 불가

#### Filter MDC 방식
```bash
./gradlew bootRun --args='--spring.profiles.active=mdc-filter'
```
성공:
- 요청 전체 생명주기에서 일관된 traceId 유지
- 모든 레이어(Filter → Controller → Service → Repository)에서 동일한 traceId 출력

---
## 🧪 테스트 실행
### 전체 테스트
```bash
./gradlew test
```

### 특정 테스트만 실행
```bash
# 스레드 이름 방식 문제 재현
./gradlew test --tests ThreadNameLoggingAspectTest

# AOP MDC 방식 문제 재현
./gradlew test --tests MdcLoggingAspectTest

# Filter 정상 동작 검증
./gradlew test --tests MdcLoggingFilterTest

# Interceptor 한계 확인
./gradlew test --tests MdcLoggingInterceptorTest
```

---
## 📊 성능 벤치마크
### JMH 벤치마크 실행
```bash
./gradlew jmh

### 측정 결과 (예시)
Benchmark                                  Mode  Cnt      Score       Error  Units
MdcPerformanceBenchmark.uuidGeneration     avgt    5    321.306 ±    64.073  ns/op
MdcPerformanceBenchmark.mdcPutAndGet       avgt    5    343.959 ±    81.284  ns/op
MdcPerformanceBenchmark.loggingWithMdc     avgt    5  16401.445 ±  2875.711  ns/op
MdcPerformanceBenchmark.loggingWithoutMdc  avgt    5  19905.075 ± 14091.645  ns/op
```
결론:
- UUID 생성(321ns)과 MDC 작업(344ns)은 나노초 단위로 매우 빠름
- MDC 사용 여부에 따른 로깅 성능 차이는 통계적 오차 범위 내
- 실제 비즈니스 로직(DB 조회 ~10ms)에 비해 무시할 수 있는 수준


---
## 🌐 API 테스트
애플리케이션 실행 후
```bash
# 로그인 API
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'

# 사용자 조회 API
curl http://localhost:8080/api/users/1

# 외부 traceId 전달
curl http://localhost:8080/api/users/1 \
  -H "X-Trace-Id: external-trace-abc123"
```

### 예상 로그 출력 (Filter 방식)
```
[a1b2c3d4] [POST /api/users/login] INFO  Request started
[a1b2c3d4] [POST /api/users/login] INFO  UserController - 로그인 요청
[a1b2c3d4] [POST /api/users/login] INFO  UserService - 사용자 인증: alice
[a1b2c3d4] [POST /api/users/login] DEBUG UserRepository - 사용자 조회: alice
[a1b2c3d4] [POST /api/users/login] INFO  Request completed - status: 200, duration: 47ms
```

---
## 🎯 핵심 학습 포인트
### 톰캣 스레드 풀의 동작 원리
- 스레드는 재사용된다 → **같은 스레드 ≠ 같은 요청**
- 100개 요청이 10개 스레드로 처리될 수 있음

### ThreadLocal의 생명주기 관리
- 스레드 풀 환경에서 `ThreadLocal.remove()` 필수
- 정리하지 않으면 이전 요청 데이터가 남아있음 (보안/메모리 이슈)

### AOP 중첩 실행의 함정
- 여러 Aspect가 중첩될 때 공유 자원(MDC) 관리 주의
- Service의 `finally { MDC.clear() }` 가 Controller에도 영향

### Filter vs Interceptor vs Aspect

|기준|Filter|Interceptor|Aspect|
|---|---|---|---|
|실행 시점|DispatcherServlet 이전|Handler 호출 전후|메서드 실행 시|
|요청당 실행|**1회**|**1회**|**N회**|
|MDC 관리|✅ 최적|△ 가능|❌ 위험|
|적용 범위|가장 넓음|중간|가장 좁음|

### 기술 선택의 원칙
> "무엇을 할 수 있는가"보다 **"무엇을 해야 하는가"** 가 더 중요하다

MDC는 요청 전체 생명주기의 컨텍스트 관리 → Filter가 정답

---
## 📚 관련 블로그 포스트
전체 개발 과정과 상세 설명:
- [MDC를 통한 요청 추적기](https://b-mokk.tistory.com/81)

포스트 내용:
- 로깅 시스템 구축 중 마주친 문제 
- 첫 번째 시도: 스레드 이름으로 구분 (실패)
- MDC 발견과 Aspect 구현 (실패)
- MDC.clear() 타이밍 문제 분석 
- 최종 해결: Filter로 이동 
- 성능 검증 (JMH 벤치마크)
- 핵심 교훈 및 확장 가능성