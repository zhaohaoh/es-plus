package com.es.plus.web.http;

/**
 * HTTP异常封装类
 * 用于统一处理HTTP请求过程中的异常
 */
public class HttpException extends RuntimeException {

    private int statusCode;
    private String errorMessage;

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorMessage = message;
    }

    public HttpException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorMessage = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "HttpException{" +
                "statusCode=" + statusCode +
                ", errorMessage='" + errorMessage + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}