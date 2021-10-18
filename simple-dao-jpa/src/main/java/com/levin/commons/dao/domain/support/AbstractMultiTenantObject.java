package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.MultiTenantObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class AbstractMultiTenantObject<ID extends Serializable>
        extends AbstractBaseEntityObject<ID>
        implements MultiTenantObject<ID> {

    private static final long serialVersionUID = -123456789L;

    @Schema(description = "租户ID")
    protected ID tenantId;

}
