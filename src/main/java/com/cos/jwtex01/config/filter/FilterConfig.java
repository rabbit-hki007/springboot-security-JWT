package com.cos.jwtex01.config.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cos.jwtex01.filtertest.MyFilter1;
import com.cos.jwtex01.filtertest.MyFilter2;

@Configuration
public class FilterConfig {  // request 가 요청될때 동작하는 필터
//  필터 테스트를 위한 코드 임 주석을 풀면서 테스트 해볼 것
//	@Bean
//	public FilterRegistrationBean<MyFilter1> filter1() {
//		
//		FilterRegistrationBean<MyFilter1> bean =  new FilterRegistrationBean<>(new MyFilter1());
//		bean.addUrlPatterns("/*");
//		bean.setOrder(1); // 낮은 번호가 필터중에서 가장 먼저 실행됨
//		return bean;
//	}
//	
//
//	@Bean
//	public FilterRegistrationBean<MyFilter2> filter2() {
//		
//		FilterRegistrationBean<MyFilter2> bean =  new FilterRegistrationBean<>(new MyFilter2());
//		bean.addUrlPatterns("/*");
//		bean.setOrder(0); // 낮은 번호가 필터중에서 가장 먼저 실행됨
//		return bean;
//	}
		
	
}
