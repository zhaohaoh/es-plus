package com.es.plus.common.json;

import com.es.plus.common.util.DateUtil;
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
    private final String timeZone;
    public EsPlusDateSerializer(String format,String timeZone) {
        this.format = format;
        this.timeZone = timeZone;
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value instanceof Long){
            jgen.writeNumber((Long)value);
            return;
        }
        Object data = DateUtil.format(value, format,timeZone);
        jgen.writeString((String) data);
    }
}