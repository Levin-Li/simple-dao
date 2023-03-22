package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.dao.domain.OrganizedObject;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConsts;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

//1、lobmok get set
@Data

//2、必须注解主键字段
//@EqualsAndHashCode(of = {"id"})

//3、必须使用链式设置
@Accessors(chain = true)

//4、必须生成常量字段
@FieldNameConstants

//5、必须注解业务名称
@Schema(description = "抽象租户组织实体")

@MappedSuperclass
public abstract class AbstractMultiTenantOrgObject
        extends AbstractMultiTenantObject
        implements MultiTenantObject, OrganizedObject {

    @Schema(description = "OrgID")
    @Column(length = 128)
    @InjectVar(InjectConsts.ORG_ID)
    protected String orgId;

}
