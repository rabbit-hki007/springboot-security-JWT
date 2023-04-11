package com.cos.jwtex01.config.jwt;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwtex01.config.auth.PrincipalDetails;
import com.cos.jwtex01.model.User;
import com.cos.jwtex01.repository.UserRepository;

// 인가
// 시큐리티가 filter를 가지고 있는데 그 필터중에 BasicAuthenticationFilter 라는 것이 있음
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음
// 만약에 권한이 인증이 필요한 주소가 아니라면 이 필터를 안탑니다
// 
public class JwtAuthorizationFilter extends BasicAuthenticationFilter{
	
	private UserRepository userRepository;
	
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
		super(authenticationManager);
		this.userRepository = userRepository;
	}
	
	
	// 인증이나 권한이 필요한 주소 요청이 있을 때 해당 필터는 무조건 타게 됨
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		//super.doFilterInternal(request,response.chain); // 이거 안지워 주면 에러 난단다 된장할
		System.out.println("인증이나 권한이 필요한 주소 요청 되어 JwtAuthorizationFilter로 왔습니다");
		
//		String Header = request.getHeader("Authorization");
//		System.out.println(Header);
		
		// JWT 토큰을 검증을 해서 정상적이 사용자인지 확인
		String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING); // "Authorization" 이 들어가 있음
		System.out.println("requestf로 넘어온 header 값 변수명 jwtHeader : " + jwtHeader);
		
		// 헤더가 있는지 확인
		// 헤더가 없으면 잘못된 것이고 header의 시작이 Bearer가 아니면 문제가 있는 헤더라서 통과 안시킴
		if(jwtHeader == null || !jwtHeader.startsWith(JwtProperties.TOKEN_PREFIX)) { //JwtProperties.TOKEN_PREFIX 는 Bearer가 들어 있음
			chain.doFilter(request, response);
                        return;
		}
		
		
		// JWT 토큰을 검증을 해서 정상적인 사용자인지 확인 아래는 순수한 키값만 추출
		String jwtToken = request.getHeader(JwtProperties.HEADER_STRING)
				.replace(JwtProperties.TOKEN_PREFIX, ""); // request.getHeader가 Authorization의 "Bearer "를 공백으로 치환시켜서 키값만 뽑아 내었음
		System.out.println("header의 Token 중 순수한 키값만 추출 : " + jwtToken);
		

		// 토큰 검증 (이게 인증이기 때문에 AuthenticationManager도 필요 없음)
		// 내가 SecurityContext에 집적접근해서 세션을 만들때 자동으로 UserDetailsService에 있는 loadByUsername이 호출됨.
		String username = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwtToken)
				.getClaim("username").asString(); //스트링으로 케스팅
		
// 아래부분은 JwtAuthenticationFilter에서 토큰을 생성한 부분을 발췌한 것임
//				String jwtToken = JWT.create()
//						.withSubject(principalDetailis.getUsername()) // 어느 값이어도 상관없다 넣고 싶은 것을 넣어주자 "tromm"
//						.withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME)) //만료시간 현재시간 - 1000분의 1초임 1000이 1총
//						.withClaim("id", principalDetailis.getUser().getId()) //withClaim이라고 되어 있는 것은 내가 넣고 싶은 값 맘데로 넣어도 된다 그래서 그냥 id와 username을 적용하였다
//						.withClaim("username", principalDetailis.getUser().getUsername())
//						.sign(Algorithm.HMAC512(JwtProperties.SECRET)); //JwtProperties.SECRET은 tromm
		
		
		
		
		
		
		if(username != null) {	
			User user = userRepository.findByUsername(username);
			
			// 인증은 토큰 검증시 끝. 인증을 하기 위해서가 아닌 스프링 시큐리티가 수행해주는 권한 처리를 위해 
			// 아래와 같이 토큰을 만들어서 Authentication 객체를 강제로 만들고 그걸 세션에 저장!
			PrincipalDetails principalDetails = new PrincipalDetails(user);
			Authentication authentication =
					new UsernamePasswordAuthenticationToken(
							principalDetails, //나중에 컨트롤러에서 DI해서 쓸 때 사용하기 편함.
							null, // 패스워드는 모르니까 null 처리, 어차피 지금 인증하는게 아니니까!!
							principalDetails.getAuthorities()); // 권한을 알려줘야 함
			
			// 강제로 시큐리티의 세션에 접근하여 Authentication 객체를 저장
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
	
		chain.doFilter(request, response); //체인을 다시타게 함
	}
	
}
