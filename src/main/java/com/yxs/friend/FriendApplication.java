package com.yxs.friend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yxs.friend.mapper")

public class FriendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FriendApplication.class, args);
    }

}
