package com.lilong.blog.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class JsonUtil {

    private static ObjectMapper om = new ObjectMapper();

    private static JsonFactory factory = new JsonFactory();

    static {
        // 配置 Jackson 支持 Java 8 时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 配置 LocalDateTime 的序列化和反序列化格式（与 @JsonFormat 保持一致）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        // 注册 JavaTimeModule
        om.registerModule(javaTimeModule);
        // 配置其他设置
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 禁用时间戳格式
        // 设置时区（与 @JsonFormat 的 timezone = "GMT+8" 对应）
        om.setTimeZone(java.util.TimeZone.getTimeZone("GMT+8"));
    }

    /**
     * @param jsonStr
     * @param clazz
     * @param <T>
     *
     * @return
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        T value = null;
        try {
            value = om.readValue(jsonStr, clazz);
        } catch (Exception e) {
            log.error("fromJson error,jsonStr:" + jsonStr + ",class:" + clazz.getName() + ",exception:"
                    + e.getMessage());
            return null;
        }
        return value;
    }

    /**
     * @param jsonStr
     * @param valueTypeRef
     * @param <T>
     *
     * @return
     */
    public static <T> T fromJson(String jsonStr, TypeReference<?> valueTypeRef) {
        T value = null;
        try {
            value = (T) om.readValue(jsonStr, valueTypeRef);
        } catch (Exception e) {
            log.error("fromJson error,jsonStr:" + jsonStr + ",class:" + valueTypeRef.getType() + ",exception:"
                    + e.getMessage());
            throw new RuntimeException(e);
        }
        return value;
    }


    public static <T> T fromJson(String jsonStr, Class<T> clazz, Class<?> clazzT) {
        JavaType javaType = om.getTypeFactory().constructParametricType(clazz, clazzT);
        try {
            return om.readValue(jsonStr, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param obj
     * @param prettyPrinter
     *
     * @return
     */
    public static String toJson(Object obj, boolean prettyPrinter) {
        if (obj == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        try {
            JsonGenerator jg = factory.createGenerator(sw);
            if (prettyPrinter) {
                jg.useDefaultPrettyPrinter();
            }
            om.writeValue(jg, obj);
        } catch (IOException e) {
            log.error("toJson error,jsonObj:" + obj.getClass().getName() + ",prettyPrinter:" + prettyPrinter
                    + ",exception:" + e.getMessage());
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    /**
     * @param object
     *
     * @return
     */
    public static String toJsonString(Object object) {
        try {
            String valueAsString = om.writeValueAsString(object);
            return valueAsString;
        } catch (Exception e) {
            log.error("toJson error,jsonObj:" + object.getClass().getName() + ",exception:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * @param object
     *
     * @return
     */
    public static String toJsonStringWithDefaultPrettyPrinter(Object object) {
        try {
            String valueAsString = om.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            return valueAsString;
        } catch (Exception e) {
            log.error("toJson error,jsonObj:" + object.getClass().getName() + ",exception:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
