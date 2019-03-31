package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * 此类用来抛自定义异常
 */
public class ExceptionCast {
    //静态方法调用
    public static void cast(ResultCode resultCode) {
        throw new CustomException(resultCode);
    }

}
