package com.xuecheng.auth.service;


import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 认证方法
     *
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //申请令牌
        AuthToken authToken = applyToken(username, password, clientId, clientSecret);
        if (authToken == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //将token储存到redis
        String access_token = authToken.getAccess_token();
        String content = JSON.toJSONString(authToken);
        boolean saveTokenResult = saveToken(access_token, content, tokenValiditySeconds);
        if (!saveTokenResult) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    /*将 token存储到redis*/
    private boolean saveToken(String access_token, String content, long tokenValiditySeconds) {
        //令牌名称
        String name = "user_token:" + access_token;
        //保存到令牌到redi
        stringRedisTemplate.boundValueOps(name).set(content, tokenValiditySeconds, TimeUnit.SECONDS);
        //获取过期时间
        Long expire = stringRedisTemplate.getExpire(name);
        return expire > 0;
    }

    /*认证方法*/
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        //选中认证服务的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        if (serviceInstance == null) {
            LOGGER.error("choose an auth instance fail");
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_AUTHSERVER_NOTFOUND);
        }
        //获取令牌的url
        String path = serviceInstance.getUri().toString() + "/auth/oauth/token";

        //定义body
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        //授权方式
        formData.add("grant_type", "password");
        //账号
        formData.add("username", username);
        //密码
        formData.add("password", password);
        //定义头

        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", httpBasic(clientId, clientSecret));

        Map map = null;
        try {
            //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
            ((RestTemplate) restTemplate).setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                public void handleError(ClientHttpResponse response) throws IOException {
                    //当响应的值为400或401时候也要正常响应，不要抛出异常
                    if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                        super.handleError(response);
                    }
                }
            });

            //http请求spring security的申请令牌接口
            ResponseEntity<Map> mapResponseEntity = restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<MultiValueMap<String, String>>(formData, header), Map.class);
            map = mapResponseEntity.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            LOGGER.error("request oauth_token_password error: {}", e.getMessage(), Throwables.getStackTraceAsString(e));

            e.printStackTrace();
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        if (map == null ||
                map.get("access_token") == null ||
                map.get("refresh_token") == null ||
                map.get("jti") == null) {
            //jti是jwt令牌的唯一标识作为用户身份令牌
            // ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);

            //获取spring security返回的错误信息
            String error_description = (String) map.get("error_description");
            if (StringUtils.isNotEmpty(error_description)) {
                if ("坏的凭证".equals(error_description)) {
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                } else if (error_description.indexOf("UserDetailsService returned null") >= 0) {
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }


        AuthToken authToken = new AuthToken();
        //访问令牌(jwt)
        String jwt_token = (String) map.get("access_token");
//刷新令牌(jwt)
        String refresh_token = (String) map.get("refresh_token");
//jti，作为用户的身份标识
        String access_token = (String) map.get("jti");
        authToken.setJwt_token(jwt_token);
        authToken.setAccess_token(access_token);
        authToken.setRefresh_token(refresh_token);
        return authToken;
    }


    /*获取httpbasic认证串*/
    private String httpBasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId + ":" + clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        byte[] encode1 = Base64Utils.encode(string.getBytes());

        System.out.println("========================" + encode1.equals(encode));
        return "Basic " + new String(encode1);

    }

    /*从redis查询令牌*/
    public AuthToken getUserToken(String token) {
        String userToken = "user_token:" + token;
        String userTokenString = stringRedisTemplate.opsForValue().get(userToken);
        if (userToken != null) {
            AuthToken authToken = null;
            try {
                authToken = JSON.parseObject(userTokenString, AuthToken.class);
            } catch (Exception e) {
                LOGGER.error("getUserToken from redis and execute JSON.parseObject error{}", e.getMessage(), Throwables.getStackTraceAsString(e));
                e.printStackTrace();
            }
            return authToken;
        }
        return null;
    }


    /*从redis中删除令牌*/
    public boolean delToken(String access_token) {
        String name = "user_token:" + access_token;
        stringRedisTemplate.delete(name);
        return true;
    }
}
