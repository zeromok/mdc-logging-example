package io.github.zeromok.benchmark;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MdcPerformanceBenchmark {

	// Logger 직접 선언 (JMH에서는 @Slf4j 안 됨)
	private static final Logger log = LoggerFactory.getLogger(MdcPerformanceBenchmark.class);

	@Benchmark
	public String uuidGeneration() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	@Benchmark
	public String mdcPutAndGet() {
		String traceId = UUID.randomUUID().toString().substring(0, 8);
		MDC.put("traceId", traceId);
		String result = MDC.get("traceId");
		MDC.clear();
		return result;
	}

	@Benchmark
	public void loggingWithMdc() {
		MDC.put("traceId", "test-1234");
		log.info("Test log message");
		MDC.clear();
	}

	@Benchmark
	public void loggingWithoutMdc() {
		log.info("Test log message");
	}
}
