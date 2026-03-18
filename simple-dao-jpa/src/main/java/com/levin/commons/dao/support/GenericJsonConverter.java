package com.levin.commons.dao.support;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.Serializable;
import java.util.function.Function;

/**
 * 通用 JSON ↔ Java 对象 转换器（支持任意可序列化的 Java 类型）
 * 无需为每个 POJO 写单独的转换器，通过泛型动态适配
 */
//@Converter // 不设置 autoApply = true，避免全局自动生效（按需绑定更灵活）
public class GenericJsonConverter<T> implements AttributeConverter<T, String> {

    // 目标转换类型（通过构造函数动态传入）
    @Getter
    private final Class<T> targetClass;

    /**
     * 构造函数
     *
     * @param targetClass 目标 POJO 的 Class 对象
     */
    public GenericJsonConverter(Class<T> targetClass) {
        // 校验参数非空，避免空指针
        Assert.notNull(targetClass, "targetClass 不能为 null");
        this.targetClass = targetClass;
    }

    /**
     * Java 对象 → JSON 字符串（写入数据库）
     */
    @Override
    public String convertToDatabaseColumn(T attribute) {

        // 空值直接返回 null
        if (attribute == null) {
            return null;
        }

        return JSON.toJSONString(attribute);
    }

    /**
     * JSON 字符串 → Java 对象（读取数据库）
     */
    @Override
    public T convertToEntityAttribute(String text) {

        if (StrUtil.isBlank(text)) {
            return null;
        }

        return JSON.parseObject(text, targetClass);

    }

}