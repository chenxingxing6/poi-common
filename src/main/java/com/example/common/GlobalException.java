package com.example.common;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * User: lanxinghua
 * Date: 2018/10/4 17:00
 * Desc:
 */
@ControllerAdvice
public class GlobalException {
    /**
     * Exception 异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, Object> defaultExceptionHandler(Exception e){
        Map<String, Object> map = new HashMap<>();
        map.put("code", 500);
        map.put("msg", e.getMessage());
        return map;
    }
}
