package com.example.Common;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    //私有函数，防止直接new
    private Result() {}
    /**
     * 成功返回结果(无数据)
     *
     *
     */
    public static Result<Void> success() {
        Result<Void> result = new Result<>();
        result.code = 1;
        result.msg="成功";
        return result;
    }

    /**
     * 成功返回结果（带数据）
     *
     * @param object 数据
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        result.msg="成功";
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param msg 提示信息
     */
    public static <T> Result<T> error(String msg,T data) {
        Result<T> result = new Result<>();
        result.msg = "登录失败，问题:"+msg;
        result.code = 0;
        result.data=data;
        return result;
    }

    /**
     * 失败返回结果
     */
    public static Result<Void> error(String msg){
        Result<Void> result = new Result<>();
        result.code=0;
        result.msg=msg;
        return result;
    }
}
