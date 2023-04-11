package com.cos.jwtex01.config.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cos.jwtex01.model.User;

import lombok.Data;

@Data
public class PrincipalDetails implements UserDetails{

	private User user;

    public PrincipalDetails(User user){
        this.user = user;
    }

    public User getUser() {
		return user;
	}

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
    
	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		// role이 여러개 일 경우
        user.getRoleList().forEach(r -> {
        	authorities.add(()->{ return r;});
        });
        return authorities;

        
//      role 이 한개 일때
//      Collection<GrantedAuthority> collect =new ArrayList<GrantedAuthority>();
//		collect.add(new GrantedAuthority() {
//			@Override
//			public String getAuthority() {
//				// String 타입으로 리턴이 가능해짐
//				return user.getRole();
//			}
//		});
//		return collect;
        // 위에 것을 간략히 람다식으로 표현하면 아래와 같다
//        Collection<GrantedAuthority> collect = new ArrayList<GrantedAuthority>();
//		collet.add(()->{ return user.getRole();});
//		return collect;
        
        
        
    }
}
