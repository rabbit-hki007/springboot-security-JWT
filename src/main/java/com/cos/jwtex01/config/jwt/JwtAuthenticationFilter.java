package com.cos.jwtex01.config.jwt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

import javax.persistence.Id;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwtex01.config.auth.PrincipalDetails;
import com.cos.jwtex01.dto.LoginRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

// 스프링 시큐리티가 가지고 있는 UsernamePasswordAuthenticationFilter 상속받아서 작성한다
// 언제 동작하는가 하면 우리가 login 요청해서 username, password POST 방식으로 전송하면
// UsernamePasswordAuthenticationFilter 동작을 함
// 그러나 이마저도 security config에서 .formLogin().disable()로 했기때문에 작동을 안하는 것인데
// 어떻게 강제로 작동시키냐 하면 아래에서 만들은 JwtAuthenticationFilter 필터를 
// Security config에 다시 등록을 해주어야 함 등록은 간단함
// SecurityConfig에 .formLogin().disable() 하단에 .addFilter(new JwtAuthenticationFilter()) 를 등록해 주면 된다)
// 이때 꼭 넘겨주어야 하는 Parameter 있는데 이게 authenticationManager() 임
// 그래서 아래와 같은 형태가 됨
// .addFilter(new JwtAuthenticationFilter(authenticationManager()))
// 그러나 JwtAuthenticationFilter는 authenticationManager()를 받을 수 없음 - 파라미터가 없음 
// 그래서 @RequiredArgsConstructor 와 private final AuthenticationManager authenticationManager; 로 생성자를 만들어 받았음
// 그리고 나서 attemptAuthentication 함수를 overrride 함
// 주소에 /login으로 인증 요청시에 실행되는 함수가 attemptAuthentication 임
// System.out.println("JwtAuthenticationFilter의 attemptAuthentication() : 진입하고 로그인 시도중입니다."); 가 찍히면 정상 진입 된 것이고
// 1. username 과 password를 받아서
// 2 정상 로그인 인지 시도해 보는 것인데 이때 가장 간단한 방법인 받아온 AuthenticationManager로 시도하는 것인데 이 시도는 
//   AuthenticationManager로 시도를 하면 자동으로 PrincipalDetailsService가 들고 있는 loadUserByUsername이 호출되어 자동 실행된다
// 3.그러면 PrincipalDetails가 각 자료를 담아 return 된 User 정보를 Session에 담아 사용(권한관리를 위해서임이게 아니면 Session에 안 담아도 됨) 하고
// 4. JWT토큰을 만들어서 응답해 주면 됨 

@RequiredArgsConstructor 
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

	private final AuthenticationManager authenticationManager;
	
	// Authentication 객체 만들어서 리턴 => 의존 : AuthenticationManager
	// 주소에 /login으로 인증 요청시에 실행되는 함수가 attemptAuthentication 임
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		
		System.out.println("JwtAuthenticationFilter의 attemptAuthentication() : 진입하고 로그인 시도중입니다.");
		
		// 1. request로 넘어온 username과 password를 받는 방법 BufferedReader
//		try {
//			//BufferedReader 로 읽어들임
//			BufferedReader br = request.getReader();
//			String input = null;
//			
//			while ((input = br.readLine()) != null) {
//				System.out.println(input);
//			}
//			
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		System.out.println("==========================================");	

		
        // 위에 BufferedReader 방식이 아니라 아래 방법처럼 ObjectMapper를 이용하여 쉽게 파싱 할수 있다
		// request에 있는 username과 password를 파싱해서 자바 Object로 받기
		ObjectMapper om = new ObjectMapper();
		LoginRequestDto loginRequestDto = null;
		try {
			loginRequestDto = om.readValue(request.getInputStream(), LoginRequestDto.class);
			//User user =om.readValue(request.getInputStream(), UsernamePasswordAuthenticationFilter.class)//LoginRequestDto를 안쓰고 User.class에 담을 때
			System.out.println("ObjectMapper om으로 담은 내용 : " + loginRequestDto.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("JwtAuthenticationFilter : "+loginRequestDto);
		
		// 유저네임과 패스워드 토큰 생성 이 생성된 토큰을 가지고 아래 authenticationManager.authenticate() 함수의 파라미터로 날림
		UsernamePasswordAuthenticationToken authenticationToken = 
				new UsernamePasswordAuthenticationToken(
						loginRequestDto.getUsername(), 
						loginRequestDto.getPassword());

		// LoginRequestDto 안쓰고 User에 담을때
//		UsernamePasswordAuthenticationToken authenticationToken =
//				new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
		
		
		System.out.println("JwtAuthenticationFilter : 토큰생성완료");
		
		//
		// 아래 것이 호출될때 뭐가 실행 되냐 하면은 PrincipalDetailsService의 loadUserByUsername을 호출하여 로그인은 시도한다
		// 그 다음 로그인이 된다면 principalDetailis를 Authentication Session에 User를 담아준다
		// authenticate() 함수가 호출 되면 인증 프로바이더가 유저 디테일 서비스의
		// loadUserByUsername(토큰의 첫번째 파라메터) 를 호출하고
		// UserDetails를 리턴받아서 토큰의 두번째 파라메터(credential)과
		// UserDetails(DB값)의 getPassword()함수로 비교해서 동일하면
		// Authentication 객체를 만들어서 필터체인으로 리턴해준다.
		
		// Tip: 인증 프로바이더의 디폴트 서비스는 UserDetailsService 타입
		// Tip: 인증 프로바이더의 디폴트 암호화 방식은 BCryptPasswordEncoder
		// 결론은 인증 프로바이더에게 알려줄 필요가 없음.
		Authentication authentication = 
				authenticationManager.authenticate(authenticationToken);
		
		// authentication 객체가 session영역에 저장됨 => 로그인이 되었다는 뜻임
		PrincipalDetails principalDetailis = (PrincipalDetails) authentication.getPrincipal(); //PrincipalDetails로 다운캐스팅
		System.out.println("Authentication : " + principalDetailis.getUser().getUsername() + "로 정상 로그인이 되었음"); // 정상 값이 출력이 된다는 것은 정상 로그인이 되었다는 이야기임
		//return super/attemptAuthentication(request, response); // authentication을 그대로 리턴해주면 된다
		
		// 아랫줄 설명 : authentication 객체가 session 영역에  저장을 해야하고 그 방법이 return authentication 해주는 것임
		// 리턴의 이유는 권한 관리를 security가 대신 해주기 때문에 편하려고 하는 것임
		// 굳이 JWT를 사용하면서 세션을 만들 이유가 없음 근데 단지 권한 처리때문에 session에 넣어 주는 것임 
		// 그런다음 리턴 되기전에 JWT를 만들어 주어도 되나 attemptAuthentication 다음 순서로 실행 되는 아래 successfulAuthentication 여기에서 만들어 주어도 됨
		return authentication;  // 리턴해 주면 Session에 저장해 준다
	}

	// 위의 attemptAuthentication 이 정상정으로 실행되고 난뒤에 실행되는 successfulAuthentication 함수임
	// 토큰을 여기서 만들거나 아니면 attemptAuthentication의 리턴 되기전에 만들어 주기도 함
	// JWT Token 생성해서 request요청한 사용자에게 토큰을 response에 담아주기
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		
		System.out.println("successfulAuthentication 실행됨 : 인증이 attemptAuthentication에서 되었다는 뜻임");
		
		PrincipalDetails principalDetailis = (PrincipalDetails) authResult.getPrincipal(); // principalDetailis 이 정보로 JWT 토큰을 만들것임
		
		// RSA 방식이 아니고 Hash암호 방식
		String jwtToken = JWT.create()
				.withSubject(principalDetailis.getUsername()) // 어느 값이어도 상관없다 넣고 싶은 것을 넣어주자 "tromm"
				.withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME)) //만료시간 현재시간 - 1000분의 1초임 1000이 1총
				.withClaim("id", principalDetailis.getUser().getId()) //withClaim이라고 되어 있는 것은 내가 넣고 싶은 값 맘데로 넣어도 된다 그래서 그냥 id와 username을 적용하였다
				.withClaim("username", principalDetailis.getUser().getUsername())
				.sign(Algorithm.HMAC512(JwtProperties.SECRET)); //JwtProperties.SECRET은 tromm
		
		response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX+jwtToken); //response.addHeader("Authorization", "Bearer "+jwtToken);
//      다음 코딩 단계		
//		이제 무엇을 해야 하냐하면은 
//		
//		세션방식에서는
//		Username과 password 로그인 정상이면
//		서버쪽 세션 ID 생성
//		클라이언트 쿠키 세션 ID를 응답
//		
//		요청할때마다 쿠키값 세션 ID를 항상 들고 서버쪽으로 요청하기 때문에
//		서버는 세션 ID가 유효한지 판단(이건 서버가 알아서 자동으로 판단해줌 session.getAttribute)해서 유효하면 인증이 필요한 페이지로
//		접근하게 하면 되요.
//		
//		그러나 JWT 방식에서는 
//		
//		세션을 사용하지 않습니다.
//		로그인이 정상적이면
//		JWT 토큰을 생성
//		클라이언트 쪽으로 JWT 토큰을 응답해 주죠
//		
//		요청할때마다 JWT 토큰을 가지고 요청을 갑니다
//		서버는 JWT 토큰이 유효한지를 판단해야 하는데 이것은 필터가 없습니다
//		그래서 이것을 만들어 줘야 합니다
		
		
		//Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJoa2kwMDciLCJpZCI6MSwiZXhwIjoxNjgyMDU2ODc4LCJ1c2VybmFtZSI6ImhraTAwNyJ9.lhARSNOPL_sENajMv7U9OQt8suVgvZlRIvWS9q9zy4TUPJfCNYAPhXcblfM3VgAJZySaciSMItVO_WImeM29Yg
		
		
		
		
	}
	
}
