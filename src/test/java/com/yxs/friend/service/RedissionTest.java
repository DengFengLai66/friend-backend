package com.yxs.friend.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author: yxs
 * @create: 2023-08-18 16:25
 **/

@SpringBootTest
public class RedissionTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void doRedissionTest(){
        RList<String> testlist = redissonClient.getList("testlist");
        //testlist.add("aaa");
        System.out.println(testlist.get(0));
        testlist.remove(0);
    }
}
