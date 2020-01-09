package com.levin.commons.dao.domain;

/**
 * 模板化的对象
 */
public interface TemplateObject<T, RULE> {

    /**
     * @return usedTemplate
     */
    T getUsedTemplate();

    /**
     * 获取模板应用规则
     *
     * @return rule
     */
    RULE getTemplateApplyRule();

}
