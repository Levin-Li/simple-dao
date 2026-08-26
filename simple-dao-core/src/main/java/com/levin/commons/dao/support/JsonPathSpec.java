package com.levin.commons.dao.support;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 *
 * $                 根节点
 * $.name            对象字段
 * $."field-name"    特殊字段名
 * $[0]              第 0 个数组元素
 * $[last]           最后一个元素
 * $[0 to 2]         数组范围
 * $[*]              所有数组元素
 * $.**              递归后代节点
 * @                 过滤器中的当前元素
 * $var              JSONPath 变量
 * ? (...)           过滤条件
 *
 *
 *
 *
 *
 */
public final class JsonPathSpec {

    private final String rawPath;
    private final boolean wildcard;
    private final boolean multiValued;

    private JsonPathSpec(String rawPath, boolean wildcard, boolean multiValued) {
        this.rawPath = rawPath;
        this.wildcard = wildcard;
        this.multiValued = multiValued;
    }

    public static JsonPathSpec parse(String rawPath) {
        Assert.hasText(rawPath, "jsonPath 不能为空");

        String path = rawPath.trim();
        Assert.isTrue(path.startsWith("$"), "jsonPath 必须以 $ 根节点开始");
        Assert.isTrue(!path.contains("'"), "jsonPath 不能包含单引号");
        boolean wildcard = path.contains("[*]");
        boolean multiValued = wildcard
                || path.contains(".**")
                || path.matches(".*\\[\\s*[^]]+\\s+to\\s+[^]]+].*")
                || path.matches(".*\\?\\s*\\(.*");
        return new JsonPathSpec(path, wildcard, multiValued);
    }

    public String getRawPath() {
        return rawPath;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public boolean isScalarOnlyAllowed() {
        return StringUtils.hasText(rawPath) && !multiValued;
    }
}
