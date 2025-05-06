package com.es.plus.adapter.util;

import com.es.plus.adapter.json.EsPlusDateSerializer;
import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.constant.EsFieldType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.apache.commons.lang3.StringUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author hzh
 * @date 2021/4/4 16:57 更新
 * Json序列化工具类  为空的列不参与序列化   以免es更新的时候多更新了null的列
 */
public class JsonUtils {

    /**
     * 定义jackson对象
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 自定义日期序列化器缓存
     */
    private static final Map<String, EsPlusDateSerializer> DATE_SERIALIZER_CACHE = new ConcurrentHashMap<>();

    /**
     * 反序列化器日期列表 常见的放前面更好
     */
    private static final List<String> DESERIALIZER_DATE_LIST = new ArrayList<String>() {{
        add("yyyy-MM-dd HH:mm:ss");
        add("yyyy-MM-dd");
        add("yyyy-MM-dd'T'HH:mm:ss'Z'");
        add("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        add("yyyy-MM-dd HH:mm");
        add("yyyy-MM-dd HH");
        add("yyyy-MM");
        add("yyyy");
    }};

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
        //定义了date类型的字段序列化会走自定义的序列化器。这里定义的4个会用不到，只是默认的。
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        MAPPER.registerModule(new EsPlusModule());
        //默认反序列化返回的时间
        MAPPER.setDateFormat(new SimpleDateFormat() {
            // 这个方法是反序列化需要滴！
            @Override
            public Date parse(String source) {
                return str2Data(source);
            }
        });

        MAPPER.registerModule(simpleModule);
    }


    /***
     * 转换字符串为日期date
     *
     * 自动匹配转化，支持DESERIALIZER_DATE_LIST中的几种格式
     */
    private static Date str2Data(String dateStr, int... fmtIndex) {
        int index = 0;
        if (fmtIndex != null && fmtIndex.length > 0) {
            index = fmtIndex[0];
        }
        if (index > DESERIALIZER_DATE_LIST.size() - 1) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(DESERIALIZER_DATE_LIST.get(index));
        try {
            Date parse = format.parse(dateStr);
            long time = parse.getTime();
            return parse;
        } catch (ParseException e1) {
            return str2Data(dateStr, ++index);
        }
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
            if (obj == null){
                return null;
            }
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // 将json数据转换成pojo对象
    public static <T> T toBean(String json, Class<T> beanType) {
        try {
            if (json == null){
                return null;
            }
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


    private static class EsPlusModule extends SimpleModule {

        private static final long serialVersionUID = -9168968092458058966L;

        /**
         * Creates a new {@link EsPlusModule} using the given
         */
        public EsPlusModule() {
            setSerializerModifier(new EsPlusSerializerModifier());
            setDeserializerModifier(new EsPlusDeserializerModifier());
        }

        /**
         * A {@link BeanSerializerModifier} that will drop properties annotated with
         *
         * @author Oliver Gierke
         * @since 3.1
         */
        private static class EsPlusDeserializerModifier extends BeanDeserializerModifier {

            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                List<BeanPropertyDefinition> beanPropertyDefinitions = new ArrayList<>(propDefs);
                propDefs.clear();
                for (BeanPropertyDefinition beanPropertyDefinition : beanPropertyDefinitions) {
                    EsField annotation = beanPropertyDefinition.getField().getAnnotation(EsField.class);
                    EsId esId = beanPropertyDefinition.getField().getAnnotation(EsId.class);
                    if (annotation != null && StringUtils.isNotBlank(annotation.name())) {
                        propDefs.add(beanPropertyDefinition.withName(PropertyName.construct(annotation.name())));
                    } else if (esId != null && StringUtils.isNotBlank(esId.name())) {
                        propDefs.add(beanPropertyDefinition.withName(PropertyName.construct(esId.name())));
                    } else {
                        propDefs.add(beanPropertyDefinition);
                    }
                }
                return propDefs;
            }
        }

        /**
         * 数据序列化器
         *
         * @author hzh
         * @date 2023/04/17
         */
        private static class EsPlusSerializerModifier extends BeanSerializerModifier {

            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription description, List<BeanPropertyWriter> properties) {
                List<BeanPropertyWriter> result = new ArrayList<>(properties.size());
                for (BeanPropertyWriter beanPropertyWriter : properties) {
                    EsField annotation = beanPropertyWriter.getAnnotation(EsField.class);
                    //如果数据不存在则不添加到序列化字段中
                    if (annotation != null && !annotation.exist()) {
                        continue;
                    }
                    //重命名字段
                    if (annotation != null && StringUtils.isNotBlank(annotation.name())) {
                        String value = annotation.name();
                        NameTransformer transformer = new NameTransformer() {
                            @Override
                            public String transform(String name) {
                                return value;
                            }

                            @Override
                            public String reverse(String transformed) {
                                return transformed;
                            }
                        };
                        beanPropertyWriter = beanPropertyWriter.rename(transformer);
                    }
                    String name1 = beanPropertyWriter.getName();
                    SerializableString serializedName = beanPropertyWriter.getSerializedName();
                    String value = serializedName.getValue();
                    //自定义date序列化
                    if (annotation != null) {
                        if (annotation.type().equals(EsFieldType.DATE)) {
                            Class<?> beanClass = description.getBeanClass();
                            String name = beanPropertyWriter.getName();
                            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(beanClass, name);
                            if (esFieldInfo != null) {
                                String dateFormat = esFieldInfo.getDateFormat();
                                EsPlusDateSerializer dateSerializer = DATE_SERIALIZER_CACHE.computeIfAbsent(dateFormat,
                                        c -> new EsPlusDateSerializer(dateFormat,esFieldInfo.getTimeZone()));
                                beanPropertyWriter.assignSerializer(dateSerializer);
                            }
                        }
                    }
                    result.add(beanPropertyWriter);
                }

                return result;
            }
        }
    }

}