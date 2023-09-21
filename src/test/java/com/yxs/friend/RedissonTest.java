package com.yxs.friend;

import com.yxs.friend.common.ResultUtil;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: redisson
 * @author: yxs
 * @create: 2023-08-06 16:37
 **/

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void tets(){
        List<String> list = new ArrayList<>();
        list.add("yxs");
        System.out.println("list"+list.get(0));
        list.remove(0);

        RList<Object> rList = redissonClient.getList("test-list");
        //rList.add("yxs");
        //System.out.println("rList"+list.get(0));
        rList.remove(0);
    }
}
