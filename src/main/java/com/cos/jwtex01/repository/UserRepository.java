package com.cos.jwtex01.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cos.jwtex01.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	User findByUsername(String username);  // PrincipalDetailsService loadUserByUsername()의 findByUsername()이 실행 되었을 때
}
