package com.yxs.friend.job;

/**
 * @author: yxs
 * @create: 2023-08-17 21:56
 **/

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yxs.friend.model.domain.User;
import com.yxs.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList = Arrays.asList(1L);
    //每天执行预热推荐用户
    //@Scheduled(cron = "0 31 0 * * *")
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("friend:user:precachejob:docache:lock");
        try {
            //tryLock代表是否抢到锁
            // tryLock(0, 30000, TimeUnit.MILLISECONDS)第一个参数是等待时间这次设置为0代表没获取到就等0秒。即不等了
            //第而个参数是过期时间，第三个参数代表单位
            boolean tryLock = lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS);
            if (tryLock){
                for (Long userId : mainUserList){
                    //查询数据库
                    QueryWrapper<User> userQueryWrapper =new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 10), userQueryWrapper);
                    String redisKey = String.format("friend:user:recommend:%s", userId);
                    ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
                    //写入缓存
                    try {
                        valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }


    }
}
