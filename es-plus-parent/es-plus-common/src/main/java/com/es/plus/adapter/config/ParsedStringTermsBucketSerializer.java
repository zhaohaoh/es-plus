package com.es.plus.adapter.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;

import java.io.IOException;

/**
 * @Author: hzh
 * @Date: 2022/6/14 12:31
 * 序列化忽略 keyAsNumber字段
 * json序列化聚合
 * 暂不使用
 */
public class ParsedStringTermsBucketSerializer extends StdSerializer<ParsedStringTerms.ParsedBucket> {

    public ParsedStringTermsBucketSerializer(Class<ParsedStringTerms.ParsedBucket> t) {
        super(t);
    }

    @Override
    public void serialize(ParsedStringTerms.ParsedBucket value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("aggregations", value.getAggregations());
        gen.writeObjectField("key", value.getKey());
        gen.writeStringField("keyAsString", value.getKeyAsString());
        gen.writeNumberField("docCount", value.getDocCount());
        gen.writeEndObject();
    }

}