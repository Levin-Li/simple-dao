package com.levin.commons.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LambdaMethodAttrTest {

    @Test
    void shouldResolvePropertyNameFromGetterMethodReference() {
        LambdaMethodAttr<DomainHolder, String> lambdaMethodAttr = DomainHolder::getDomain;

        assertEquals("domain", lambdaMethodAttr.getAttrName());
    }

    @Test
    void shouldResolvePropertyNameFromWrappedGetterMethodReference() {
        LambdaMethodAttr<DomainHolder, String> getter = DomainHolder::getDomain;
        LambdaMethodAttr<DomainHolder, String> lambdaMethodAttr = getter::apply;

        assertEquals("domain", lambdaMethodAttr.getAttrName());
    }

    static class DomainHolder {
        private String domain;

        public String getDomain() {
            return domain;
        }
    }
}
