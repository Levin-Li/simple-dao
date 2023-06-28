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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

//1、lobmok get set
@Data

//2、必须注解主键字段
//@EqualsAndHashCode(of = {"id"})

//3、必须使用链式设置
@Accessors(chain = true)

//4、必须生成常量字段
@FieldNameConstants

//5、必须注解业务名称
@Schema(title = "抽象租户组织实体")

@MappedSuperclass
public abstract class SimpleTenantOrgObject
        implements MultiTenantObject, OrganizedObject {

    @Schema(title = "租户ID")
    @Column(length = 128)
    @InjectVar(InjectConsts.TENANT_ID)
    protected String tenantId;

    @Schema(title = "组织机构ID")
    @Column(length = 128)
    @InjectVar(InjectConsts.ORG_ID)
    protected String orgId;

    @Schema(title = "创建时间")
    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date createTime;

}
