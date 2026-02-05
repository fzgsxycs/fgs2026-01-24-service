package com.example.computerassociation.common;

import com.example.computerassociation.exception.TraceIdUtil;
import lombok.Data;

/**
 * 统一API返回结果包装类
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private String TraceId;

    private Result() {}

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> Result<T> fail() {
        return fail(500, "操作失败");
    }



    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setTraceId(TraceIdUtil.getTraceId());
        return result;
    }




}