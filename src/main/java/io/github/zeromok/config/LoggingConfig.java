package io.github.zeromok.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.github.zeromok.aspect.MdcLoggingAspect;
import io.github.zeromok.filter.MdcLoggingFilter;
import io.github.zeromok.aspect.ThreadNameLoggingAspect;
import io.github.zeromok.interceptor.MdcLoggingInterceptor;

@Configuration
public class LoggingConfig {

	@Bean
	@Profile("thread-name")
	public ThreadNameLoggingAspect threadNameLoggingAspect() {
		return new ThreadNameLoggingAspect();
	}

	@Bean
	@Profile("mdc-aspect")
	public MdcLoggingAspect mdcLoggingAspect() {
		return new MdcLoggingAspect();
	}

	@Bean
	@Profile("mdc-interceptor")
	public MdcLoggingInterceptor mdcLoggingInterceptor() {
		return new MdcLoggingInterceptor();
	}

	@Bean
	@Profile("mdc-filter")
	public MdcLoggingFilter mdcLoggingFilter() {
		return new MdcLoggingFilter();
	}
}
