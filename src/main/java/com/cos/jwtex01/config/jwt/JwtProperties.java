package com.cos.jwtex01.config.jwt;

public interface JwtProperties {
	String SECRET = "tromm"; // 우리 서버만 알고 있는 비밀값
	int EXPIRATION_TIME = 864000000; // 10일 (1/1000초)
	String TOKEN_PREFIX = "Bearer "; // 무조건 뒤를 한칸 띄워야함 
	String HEADER_STRING = "Authorization";
}
