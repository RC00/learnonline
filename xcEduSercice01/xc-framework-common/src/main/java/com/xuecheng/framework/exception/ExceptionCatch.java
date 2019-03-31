package com.xuecheng.framework.exception;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 此类用来捕获异常
 */
@ControllerAdvice
public class ExceptionCatch {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ExceptionCatch.class);

    //使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
    private static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;

    //使用builder来构建一个异常类型和错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder = ImmutableMap.builder();

    static {
        //在这里加入一些基础的异常类型判断
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
    }



    /**
     * 捕获customException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException e) {
        //LOGGER.error("catch exception : {" + e.getMessage() + "}");
        LOGGER.error("catch exception:{}",e.getMessage(), Throwables.getStackTraceAsString(e));
        ResultCode resultCode = e.getResultCode();
        ResponseResult responseResult = new ResponseResult(resultCode);
        return responseResult;
    }

    /**
     * 捕获不可预知异常处理
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseResult exception(Exception e) {
        //记录日志
        LOGGER.error("catch exception:{}",e.getMessage(), Throwables.getStackTraceAsString(e));
        if (EXCEPTIONS == null) {
            EXCEPTIONS = builder.build();//EXCEPTIONS构建成功
        }
        //从EXCEPTIONS中找异常类型所对应的错误代码，如果找到了将错误代码响应给用户，如果找不到给用户响应99999异常
        final ResultCode resultCode = EXCEPTIONS.get(e.getClass());
        final ResponseResult responseResult;
        if (resultCode != null) {
            responseResult = new ResponseResult(resultCode);
        } else {
            responseResult = new ResponseResult(CommonCode.SERVER_ERROR);
        }
        return responseResult;
    }

}
