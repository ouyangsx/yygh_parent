package com.atguigu.yygh.common.handler;

import com.atguigu.yygh.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常处理类
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R error(Exception e) {
        e.printStackTrace();
        return R.error();
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public R error(ArithmeticException e) {
        e.printStackTrace();
        return R.error().message("发生了其他异常");
    }

    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public R error(YyghException e) {
        log.error(e.getMessage());
        return R.error().code(e.getCode()).message(e.getMsg());
    }
}
