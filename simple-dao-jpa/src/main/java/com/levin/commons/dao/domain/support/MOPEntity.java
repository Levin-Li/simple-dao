package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.*;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;


/**
 * 租户+组织+个人 支持共享的实体
 */
@Data
@Accessors(chain = true)
@MappedSuperclass
public abstract class MOPEntity extends AbstractMultiTenantOrgObject
        implements MultiTenantPublicObject, MultiTenantSharedObject, OrganizedPublicObject, OrganizedSharedObject, PersonalObject {

    @Schema(title = "是否租户之间共享", description = "")
    @Column(nullable = false)
    protected boolean tenantShared;

    @Schema(title = "是否组织之间共享", description = "")
    @Column(nullable = false)
    protected boolean orgShared;

    @Schema(title = "个人所有者ID", description = "数据或是记录的所有者ID，通常是用户ID")
    @Column(length = 64)
    @InjectVar(value = InjectConst.USER_ID, isRequired = "false")
    protected String ownerId;

    @Schema(title = "个人所有者名称", description = "数据或是记录的所有者ID，通常是用户ID")
    @Column
    @InjectVar(value = InjectConst.USER_NAME, isRequired = "false")
    protected String ownerName;

}
