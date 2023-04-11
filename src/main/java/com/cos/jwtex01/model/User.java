package com.cos.jwtex01.model;

import javax.persistence.*;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String username;
    private String password;
    private String roles; //USER, ADMIN 

    // role이 하나의 유저당 2개 이상일때 
    //ENUM으로 안하고 ,로 해서 구분해서 ROLE을 입력 -> 그걸 파싱!! 
    public List<String> getRoleList(){
        if(this.roles.length() > 0){
            return Arrays.asList(this.roles.split(","));
        }
        return new ArrayList<>();  // null 방지를 위해서 넣은 코드
    }
}
