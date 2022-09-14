package com.es.plus.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author hzh
 * @date 2021/4/4 16:57 更新
 * Json序列化工具类  为空的列不参与序列化   以免es更新的时候多更新了null的列
 */
public class JsonUtils {
    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        //在解析json的时候忽略字段名字不对应的会报错的情况  如usernamexxx字段映射到User实体类
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //为空的列不参与序列化   以免es更新的时候多更新了null的列
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 忽略 transient 修饰的属性
        MAPPER.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        //解决jackson2无法反序列化LocalDateTime的问题
//        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        //只序列化字段，
        MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常  指定了类型会写入序列化类的类型，这样不能通用的反序列化
//        MAPPER.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        MAPPER.setDateFormat(format);
        MAPPER.registerModule(simpleModule);
    }


    public static <T> T mapToBean(Map<String, Object> source, Class<T> targetType) {
        try {
            String json = toJsonStr(source);
            T t = toBean(json, targetType);
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // 将对象转换成json字符串
    public static String toJsonStr(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // 将json数据转换成pojo对象
    public static <T> T toBean(String json, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(json, beanType);
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Map<String, Object> toMap(String json) {
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Map<String, Object> beanToMap(Object bean) {
        try {
            String json = MAPPER.writeValueAsString(bean);
            return toMap(json);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // 将byte数组转对象
    public static <T> T bytesToBean(byte[] bytes, Class<T> beanType) {
        String json = new String(bytes);

        try {
            T t = MAPPER.readValue(json, beanType);
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // 将json数据转换成pojo对象list
    public static <T> List<T> toList(String json, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            return MAPPER.readValue(json, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}