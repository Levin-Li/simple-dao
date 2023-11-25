package com.levin.oak.base;

import com.levin.commons.utils.ExpressionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 *  SpelUtils工具类
 *  用于spel表达式
 *
 *  @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Slf4j
@Component("spelUtils")
public class SpelUtils {
    @PostConstruct
    void init() {
        log.info("init...");
    }

    public static boolean isNotEmpty(Object object) {
        return ExpressionUtils.isNotEmpty(object);
    }

    public boolean isEmpty(Object object) {
        return ExpressionUtils.isEmpty(object);
    }
}
