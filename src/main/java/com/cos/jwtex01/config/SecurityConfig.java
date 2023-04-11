package com.cos.jwtex01.config;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.cos.jwtex01.config.jwt.JwtAuthenticationFilter;
import com.cos.jwtex01.config.jwt.JwtAuthorizationFilter;
import com.cos.jwtex01.filtertest.MyFilter3;
import com.cos.jwtex01.repository.UserRepository;

@Configuration
@EnableWebSecurity // 시큐리티 활성화 -> 기본 스프링 필터체인에 등록
public class SecurityConfig extends WebSecurityConfigurerAdapter{	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CorsConfig corsConfig;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		// 필터 테스트 시작
		// 아래처럼 필터를 등록하면 에러가 발생 왜냐하면 Security 필터가 아니고 그냥 필터기 때문에 chain 으로 연결 불가함
		// http.addFilter(new MyFilter1());  
		// 그래서 시큐리티 필터 체인중 어느 필터의 앞이나 뒤에 걸어 달라고 에러 발생함
		// 그래서 아래처럼 필터를 걸어준다
		//http.addFilterBefore(new MyFilter3(), BasicAuthenticationFilter.class); // 잘 걸린다
		// 그러나 이렇게 하지도 않는다
		// jwt 패키지를 참조하라 별도로 @Configration 해준다
		
		// 여기부터가 시큐리티 셋팅의 시작입니다 
		http.csrf().disable();
		http
		         // addFilter(corsConfig.corsFilter())  모든 요청은 이 필터를 타게 된다 
		         // 이렇게 하면 서버는 모든 CrossOrign으로 부터 자유롭게 된다 (모두 허용)
		         // @Controller 혹은 @RestController에 @CorossOrigin 이라는 어노테이션을 써도 되지만
		         // 이렇게 하면 시큐리티 인증이 필요한 것들이 거부된다 그래서 필터를 만들고 등록해서 쓰는 것임
				.addFilter(corsConfig.corsFilter()) // @CrossOrigin(인증이 없을때 사용) , 필터에 등록은 (인증이 있을때)
				// 웹은 원래 Stateless 방식이다 그래서 Stateful 하게 사용하기 위해 Session 과 Cookie를 사용한다
				// Session을 사용하용하지 않겠다
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  
			.and()
				.formLogin().disable()
				.httpBasic().disable()  // 요청할때마다 Authorization : ID, PW를 달고 요청을 함 그런데 이게 노출이 될수 있음
				// 그래서 보통 https 서버를 씀 그러나 우리는 이 Authorization에 토큰을 달아서 보낼것이고 이건 노출되어도 위험부담이 적음(bearer 방식)
				
				.addFilter(new JwtAuthenticationFilter(authenticationManager())) // 필터를 걸어줌 AuthenticationManager(인증) 를 가지고 있음
				.addFilter(new JwtAuthorizationFilter(authenticationManager(), userRepository)) // 인가
				//.addFilter(new JwtAuthorizationFilter(authenticationManager()) // 인가
				.authorizeRequests()
				.antMatchers("/api/v1/user/**").access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
				.antMatchers("/api/v1/manager/**").access("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
				.antMatchers("/api/v1/admin/**").access("hasRole('ROLE_ADMIN')")
				.anyRequest().permitAll();
	}
}






