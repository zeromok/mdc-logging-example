package io.github.zeromok.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.zeromok.interceptor.MdcLoggingInterceptor;

@Configuration
@Profile("mdc-interceptor")
public class WebMvcConfig implements WebMvcConfigurer {

	private final MdcLoggingInterceptor mdcLoggingInterceptor;

	public WebMvcConfig(MdcLoggingInterceptor mdcLoggingInterceptor) {
		this.mdcLoggingInterceptor = mdcLoggingInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(mdcLoggingInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns("/actuator/**");
	}
}
