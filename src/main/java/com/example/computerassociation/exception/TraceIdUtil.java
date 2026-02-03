package com.example.computerassociation.exception;


import cn.hutool.core.util.IdUtil;

public class TraceIdUtil {

    private static final ThreadLocal<String> TRACE_ID_THREAD_LOCAL=new ThreadLocal<>();

    public static void generateAndSetTraceId(){
        String traceId=IdUtil.fastSimpleUUID();
        TRACE_ID_THREAD_LOCAL.set(traceId);
    }
    public static String getTraceId(){
        return TRACE_ID_THREAD_LOCAL.get();
    }
    public static void removeTraceId(){
        TRACE_ID_THREAD_LOCAL.remove();
    }
}
