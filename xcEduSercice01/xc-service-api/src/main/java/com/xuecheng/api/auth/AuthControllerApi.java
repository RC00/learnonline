package com.xuecheng.api.auth;


import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "用户认证//jwt查询接口", description = "用户认证接口//客户端查询jwt令牌内容")
public interface AuthControllerApi {

    @ApiOperation("登录")
    public LoginResult login(LoginRequest loginRequest);

    @ApiOperation("退出")
    public ResponseResult logout();

    @ApiOperation("查询userjwt令牌")
    public JwtResult userJwt();
}
