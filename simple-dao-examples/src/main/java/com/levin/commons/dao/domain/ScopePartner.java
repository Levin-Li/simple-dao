package com.levin.commons.dao.domain;

import com.levin.commons.dao.EntityOption;
import jakarta.persistence.*;
import lombok.Data;

/** Executable example for application-provided mandatory enum scopes. */
@Data
@Entity
@EntityOption(logicalDeleteFieldName = "deleted", logicalDeleteValue = "true", disableActions = EntityOption.Action.Delete)
public class ScopePartner {
    public enum Kind implements com.levin.commons.service.domain.EnumDesc {
        Customer, Supplier;
        @Override public int code() { return 40 + ordinal(); }
        @Override public String toString() { return this == Customer ? "客户" : "供应商"; }
    }
    public enum Region { East, South }
    public enum Rating implements com.levin.commons.service.domain.EnumDesc {
        Gold, Silver;
        @Override public int code() { return 80 + ordinal(); }
        @Override public String toString() { return this == Gold ? "金牌" : "银牌"; }
    }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) private Kind kind;
    @Enumerated(EnumType.STRING) private Region region;
    @Enumerated(EnumType.ORDINAL) private Rating rating;
    private String name;
    private Boolean deleted = false;
}
