package com.xuecheng.auth.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis() {
        //定义key
        String key = "user_token:9734b68f‐cf5e‐456f‐9bd6‐df578c711390";
        //定义map
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("id", "101");
        stringMap.put("username", "itcast");
        String value = JSON.toJSONString(stringMap);
        stringRedisTemplate.boundValueOps(key).set(value, 160, TimeUnit.SECONDS);
    }

    @Test
    public void testRedis1() {
        String key = "user_token:9734b68f‐cf5e‐456f‐9bd6‐df578c711390";

        //读取过期时间，已过期返回‐2
        Long expire = stringRedisTemplate.getExpire(key);
        System.out.println(expire);
        //根据key获取value
        String s = stringRedisTemplate.opsForValue().get(key);
        System.out.println("------------------------------------"+s);
    }


}
