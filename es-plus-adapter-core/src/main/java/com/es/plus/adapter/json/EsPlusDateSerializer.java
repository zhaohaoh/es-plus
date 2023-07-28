package com.es.plus.adapter.json;

import com.es.plus.adapter.util.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 自定义日期序列化器 支持date和localDateTime
 *
 * @author hzh
 * @date 2023/07/26
 */
public class EsPlusDateSerializer extends JsonSerializer<Object> {
    private final String format;

    public EsPlusDateSerializer(String format) {
        this.format = format;
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        Object data = DateUtil.format(value, format);
        jgen.writeString((String) data);
    }
}