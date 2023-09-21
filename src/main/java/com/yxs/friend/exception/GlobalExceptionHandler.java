package com.yxs.friend.exception;

import com.yxs.friend.common.BaseResponse;
import com.yxs.friend.common.ErrorCode;
import com.yxs.friend.common.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("businessException:"+e.getMessage(),e);
        return ResultUtil.error(e.getCode(),e.getMessage(),e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(BusinessException e){
        log.error("businessException:",e);
        return ResultUtil.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }
}
