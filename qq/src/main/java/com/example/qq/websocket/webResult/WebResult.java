package com.example.qq.websocket.webResult;

public class WebResult<T> {
    private Integer code;
    private String message;
    private T data;

    public WebResult(Integer code, String message, T data) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "WebResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    public static <T> WebResult<T> success(T data) {
        return new WebResult<T>(200, "操作成功", data);
    }
    public static <T> WebResult<T> error(T t) {
        return new WebResult<T>(300, "操作失败", t);
    }

}
