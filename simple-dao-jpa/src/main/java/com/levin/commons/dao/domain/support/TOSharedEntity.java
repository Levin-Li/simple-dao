package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.*;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;


/**
 * 租户+组织 支持共享的实体
 */
@Data
@Accessors(chain = true)
@MappedSuperclass
public abstract class TOSharedEntity extends AbstractMultiTenantOrgObject
        implements
        MultiTenantPublicObject,
        MultiTenantSharedObject,
        OrganizedPublicObject,
        OrganizedSharedObject {

    @Schema(title = "是否租户之间共享", description = "")
    @Column(nullable = false)
    protected boolean tenantShared;

    @Schema(title = "是否组织之间共享", description = "")
    @Column(nullable = false)
    protected boolean orgShared;

}
