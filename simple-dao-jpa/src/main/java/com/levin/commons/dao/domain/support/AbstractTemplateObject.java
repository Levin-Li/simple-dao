package com.levin.commons.dao.domain.support;


import com.levin.commons.dao.domain.EntityObject;
import com.levin.commons.dao.domain.TemplateObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
//@Data
//@Accessors(chain = true)
@Desc("模板对象")
@FieldNameConstants
public abstract class AbstractTemplateObject<ID extends Serializable, TEMPLATE extends EntityObject<ID>>
        extends AbstractNamedEntityObject<ID>
        implements TemplateObject<TEMPLATE, String> {

    private static final long serialVersionUID = -123456789L;

    @Desc("应用的模板")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    protected TEMPLATE usedTemplate;

    @Desc("模板ID")
    @Column(name = "template_id")
    protected ID templateId;

    @Desc("模板应用规则")
    @Column(name = "template_apply_rule")
    @Lob
    protected String templateApplyRule;

}
