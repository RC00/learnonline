package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Service
public class AuthService {

    /*由于令牌存储时采用String序列化策略，所以这里用 StringRedisTemplate来查询，使用RedisTemplate无法完成查询。*/
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    //查询身份令牌
    public String getTokenFromCookie(HttpServletRequest request) {
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        if (StringUtils.isEmpty(access_token)) {
            return null;
        }
        return access_token;
    }

    //从header中查询jwt令牌
    public String getJwtFromHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)) {
            //拒绝访问
            return null;
        }
        return authorization;
    }

    //查询令牌的有效期
    public long getExpire(String access_token) {
        //token在redis中的key
        String key = "user_token" + access_token;
        Long expire = stringRedisTemplate.getExpire(key);
        return expire;
    }
}
