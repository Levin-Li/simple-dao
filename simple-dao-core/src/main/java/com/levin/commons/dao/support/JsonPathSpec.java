package com.levin.commons.dao.support;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public final class JsonPathSpec {

    private final String rawPath;
    private final boolean wildcard;

    private JsonPathSpec(String rawPath, boolean wildcard) {
        this.rawPath = rawPath;
        this.wildcard = wildcard;
    }

    public static JsonPathSpec parse(String rawPath) {
        Assert.hasText(rawPath, "jsonPath 不能为空");

        String path = rawPath.trim();
        boolean wildcard = path.contains("[*]");
        return new JsonPathSpec(path, wildcard);
    }

    public String getRawPath() {
        return rawPath;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public boolean isScalarOnlyAllowed() {
        return StringUtils.hasText(rawPath) && !wildcard;
    }
}
