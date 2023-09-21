package com.yxs.friend.service;

import com.yxs.friend.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author: yxs
 * @create: 2023-08-17 20:47
 **/

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void testre(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("strng","aaaa");
        valueOperations.set("inte",1);
        valueOperations.set("dou",2.2);
        User user = new User();
        user.setId(6L);
        user.setUsername("Y");
        valueOperations.set("user",user);

        //查
        Object strng = valueOperations.get("strng");
        Assertions.assertTrue(strng.equals((String)"aaaa"));
        Object inte = valueOperations.get("inte");
        Assertions.assertTrue(inte.equals((Integer)1));
        Object dou = valueOperations.get("dou");
        Assertions.assertTrue(dou.equals((Double)2.2));
        Object user1 = valueOperations.get("user");
        System.out.println(user1);

        //改
        valueOperations.set("inte",666);

    }
}
