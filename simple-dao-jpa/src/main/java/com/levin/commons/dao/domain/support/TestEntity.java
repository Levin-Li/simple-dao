package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.domain.*;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

@Entity(name = "simple_dao_test_entity")
//@DynamicInsert
//@DynamicUpdate
@Data
@EqualsAndHashCode(of = {"id"})
@Accessors(chain = true)
@FieldNameConstants
@EntityOption(disableActions = {EntityOption.Action.Delete}, logicalDeleteFieldName = "deleted", logicalDeleteValue = "true")
public class TestEntity
        extends AbstractTreeObject<Long, TestEntity>
        implements
        //   MultiTenantObject,
        MultiTenantPublicObject,
        MultiTenantSharedObject,

        //  OrganizedObject,
        OrganizedPublicObject,
        OrganizedSharedObject,

        StatefulObject {

    @Id
    @GeneratedValue
    private Long id;

    @Schema(title = "租户ID")
    @Column(length = 128)
    @InjectVar(InjectConst.TENANT_ID)
    protected String tenantId;

    @Schema(title = "组织ID")
    @Column(length = 128)
    @InjectVar(InjectConst.ORG_ID)
    protected String orgId;

    @Schema(title = "是否租户之间共享")
    protected Boolean tenantShared;

    @Schema(title = "是否组织之间共享")
    protected Boolean orgShared;

    @Schema(title = "父ID")
    @Column(length = 128)
    protected Long parentId;

    @Desc("删除状态")
    @Column(nullable = false)
    boolean deleted;

    @Desc("状态")
    @Column(nullable = false)
    String state = "C";

    @Desc("类别")
    String category;

    @Desc("分数")
    Integer score;

    @Override
    @Transient
    public boolean isTenantShared() {
        return Boolean.TRUE.equals(tenantShared);
    }

    @Override
    @Transient
    public boolean isOrgShared() {
        return Boolean.TRUE.equals(orgShared);
    }
}
