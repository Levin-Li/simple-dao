package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.domain.DeletableObject;
import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.service.domain.Desc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "simple_dao_test_entity")
//@DynamicInsert
//@DynamicUpdate
@Data
@EqualsAndHashCode(of = {"id"})
@Accessors(chain = true)
@FieldNameConstants
@EntityOption(disableActions = {EntityOption.Action.Delete}, logicalDeleteFieldName = "isDeleted", logicalDeleteValue = "deleted")
public class TestEntity
        extends AbstractTreeObject<Long, TestEntity>
        implements StatefulObject, DeletableObject {

    @Id
    @GeneratedValue
    private Long id;

    @Schema(description = "父ID")
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

}
