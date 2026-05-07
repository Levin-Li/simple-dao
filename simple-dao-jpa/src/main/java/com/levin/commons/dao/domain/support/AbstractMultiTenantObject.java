package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.rbac.DataMasking;
import com.levin.commons.rbac.RbacRoleInfo;
import com.levin.commons.rbac.ResAuthorize;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * @author lilw
 */

//1、lobmok get set
@Data

//2、必须注解主键字段
//@EqualsAndHashCode(of = {"id"})

//3、必须使用链式设置
@Accessors(chain = true)

//4、必须生成常量字段
@FieldNameConstants

//5、必须注解业务名称
@Schema(title = "抽象租户实体")

@MappedSuperclass
public abstract class AbstractMultiTenantObject
        extends AbstractBaseEntityObject
        implements MultiTenantObject {

    @Schema(title = "租户ID")
    @Column(length = 128)
    @InjectVar(InjectConst.TENANT_ID)
    @DataMasking(showAuthorize = @ResAuthorize(anyRoles = {RbacRoleInfo.SA_ROLE, RbacRoleInfo.SAAS_ROLE_PREFIX + "*"}), remark = "超级管理员才能显示原内容, @CopyToGenCode @*")
    protected String tenantId;

}
