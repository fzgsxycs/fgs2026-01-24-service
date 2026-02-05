package com.example.computerassociation.exception;

public class TraceIdUtil {

    private static final ThreadLocal<String> TRACE_ID_THREAD_LOCAL=new ThreadLocal<>();

    public static String getTraceId(){
        return TRACE_ID_THREAD_LOCAL.get();
    }

}
