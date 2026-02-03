package com.example.computerassociation.exception;


import cn.hutool.core.util.IdUtil;

public class TraceIdUtil {

    private static final ThreadLocal<String> TRACE_ID_THREAD_LOCAL=new ThreadLocal<>();

    public static String getTraceId(){
        return TRACE_ID_THREAD_LOCAL.get();
    }

}
