package com.es.plus.util;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings({"unchecked", "all"})
public class XcontentBuildUtils {

    public static Map<String, Object> buildAnalyzer(String type, String[] filters, String tokenizer) {
        XContentBuilder xContentBuilder = null;
        try {
            xContentBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("filter", filters)
                    .field("type", type)
                    .field("tokenizer", tokenizer)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BytesReference.bytes(xContentBuilder);
        return JsonUtils.toMap(xContentBuilder.getOutputStream().toString());
    }

}
