package com.cos.jwtex01.config.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cos.jwtex01.model.User;
import com.cos.jwtex01.repository.UserRepository;

import lombok.RequiredArgsConstructor;


// http://localjhost:8080/login 이라고 요청 되었을때
// 해당 service가 동작되어야 하지만 jwt 로그인 방식에서는 위 주소로 접근하면 404에러가 뜬다
// 왜냐하면 Spring Security에서 .formLogin().disable()로 설정하였기 때문
// 그래서 우리는 PrincipalDetailsService를 강제로 실행하기 위한 filter를 만들어 실행해 주어야 한다
// 그 필터가 JwtAuthenticationFilter 이다
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService{

	private final UserRepository userRepository;

	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("PrincipalDetailsService의 loadUserByUsername() : 진입");
		User user = userRepository.findByUsername(username);

		// session.setAttribute("loginUser", user);
		return new PrincipalDetails(user);
	}
	
    
}
