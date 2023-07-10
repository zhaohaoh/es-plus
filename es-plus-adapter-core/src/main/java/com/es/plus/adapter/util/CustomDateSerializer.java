
package com.es.plus.adapter.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateSerializer extends JsonSerializer<Date> {
  
    @Override  
    public void serialize(Date value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        jgen.writeString( date2str(value));
  
    }
    /***
     * date格式化字符串
     * @param date
     * @return
     */
    private   String date2str(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z' || yyyy-MM-dd HH:mm:ss || yyyy-MM-dd || yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String str = format.format(date);
        return str;
    }
} 