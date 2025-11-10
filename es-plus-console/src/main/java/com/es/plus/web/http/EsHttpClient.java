package com.es.plus.web.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Elasticsearch HTTP客户端封装类
 * 基于OkHttp实现，提供简洁的HTTP API调用接口
 */
@Slf4j
@Component
public class EsHttpClient {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    @Autowired
    public EsHttpClient(HttpClientConfig config) {
        this.objectMapper = new ObjectMapper();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
                .connectionPool(config.createConnectionPool())
                .retryOnConnectionFailure(true)
                .addInterceptor(config.createLoggingInterceptor());

        this.client = builder.build();
    }

    /**
     * GET请求
     */
    public String get(String url) throws HttpException {
        return get(url, null, null);
    }

    /**
     * GET请求（带请求头）
     */
    public String get(String url, Map<String, String> headers) throws HttpException {
        return get(url, headers, null);
    }

    /**
     * GET请求（带请求头和查询参数）
     */
    public String get(String url, Map<String, String> headers, Map<String, String> params) throws HttpException {
        try {
            if (params != null && !params.isEmpty()) {
                url = buildUrlWithParams(url, params);
            }

            Request.Builder requestBuilder = new Request.Builder().url(url);
            addHeaders(requestBuilder, headers);

            Request request = requestBuilder.build();

            log.debug("发送GET请求: {}", url);

            try (Response response = client.newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            log.error("GET请求失败: {}", url, e);
            throw new HttpException("GET请求失败: " + url, e);
        }
    }

    /**
     * POST请求
     */
    public String post(String url, String body) throws HttpException {
        return post(url, body, null);
    }

    /**
     * POST请求（带请求头）
     */
    public String post(String url, String body, Map<String, String> headers) throws HttpException {
        try {
            RequestBody requestBody = RequestBody.create(body != null ? body : "",
                    MediaType.get("application/json; charset=utf-8"));

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(requestBody);

            addHeaders(requestBuilder, headers);

            Request request = requestBuilder.build();

            log.debug("发送POST请求: {}, Body: {}", url, body);

            try (Response response = client.newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            log.error("POST请求失败: {}", url, e);
            throw new HttpException("POST请求失败: " + url, e);
        }
    }

    /**
     * POST请求（对象序列化）
     */
    public String post(String url, Object body) throws HttpException {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            return post(url, jsonBody, null);
        } catch (JsonProcessingException e) {
            log.error("对象序列化失败: {}", body, e);
            throw new HttpException("对象序列化失败", e);
        }
    }

    /**
     * PUT请求
     */
    public String put(String url, String body) throws HttpException {
        return put(url, body, null);
    }

    /**
     * PUT请求（带请求头）
     */
    public String put(String url, String body, Map<String, String> headers) throws HttpException {
        try {
            RequestBody requestBody = RequestBody.create(body != null ? body : "",
                    MediaType.get("application/json; charset=utf-8"));

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .put(requestBody);

            addHeaders(requestBuilder, headers);

            Request request = requestBuilder.build();

            log.debug("发送PUT请求: {}, Body: {}", url, body);

            try (Response response = client.newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            log.error("PUT请求失败: {}", url, e);
            throw new HttpException("PUT请求失败: " + url, e);
        }
    }

    /**
     * DELETE请求
     */
    public String delete(String url) throws HttpException {
        return delete(url, null);
    }

    /**
     * DELETE请求（带请求头）
     */
    public String delete(String url, Map<String, String> headers) throws HttpException {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .delete();

            addHeaders(requestBuilder, headers);

            Request request = requestBuilder.build();

            log.debug("发送DELETE请求: {}", url);

            try (Response response = client.newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            log.error("DELETE请求失败: {}", url, e);
            throw new HttpException("DELETE请求失败: " + url, e);
        }
    }

    /**
     * DELETE请求（带请求体和请求头）
     */
    public String delete(String url, String body, Map<String, String> headers) throws HttpException {
        try {
            RequestBody requestBody = RequestBody.create(body != null ? body : "",
                    MediaType.get("application/json; charset=utf-8"));

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .delete(requestBody);

            addHeaders(requestBuilder, headers);

            Request request = requestBuilder.build();

            log.debug("发送DELETE请求(带body): {}", url);

            try (Response response = client.newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            log.error("DELETE请求失败: {}", url, e);
            throw new HttpException("DELETE请求失败: " + url, e);
        }
    }

    /**
     * 处理HTTP响应
     */
    private String handleResponse(Response response) throws HttpException {
        int statusCode = response.code();
        String responseBody = null;

        try {
            responseBody = response.body() != null ? response.body().string() : "";

            log.debug("收到响应: Status={}, Body={}", statusCode,
                    responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody);

            if (!response.isSuccessful()) {
                throw new HttpException(statusCode, "HTTP请求失败: " + statusCode + ", Body: " + responseBody);
            }

            return responseBody;
        } catch (IOException e) {
            log.error("读取响应体失败", e);
            throw new HttpException("读取响应体失败", e);
        }
    }

    /**
     * 添加请求头
     */
    private void addHeaders(Request.Builder builder, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 构建带查询参数的URL
     */
    private String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        boolean hasQuery = baseUrl.contains("?");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!hasQuery) {
                urlBuilder.append("?");
                hasQuery = true;
            } else {
                urlBuilder.append("&");
            }
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return urlBuilder.toString();
    }
}